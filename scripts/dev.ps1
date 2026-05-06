Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $repoRoot "backend"
$frontendDir = Join-Path $repoRoot "enterprise-insight-backend-react"
$logDir = if ($env:EIP_LOG_DIR) { $env:EIP_LOG_DIR } else { Join-Path $repoRoot "runtime-logs" }
$frontendPort = if ($env:FRONTEND_PORT) { $env:FRONTEND_PORT } else { "5173" }
$backendStartDelay = if ($env:EIP_BACKEND_START_DELAY) { [int]$env:EIP_BACKEND_START_DELAY } else { 2 }

New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$processes = New-Object System.Collections.Generic.List[System.Diagnostics.Process]

function Resolve-Tool {
    param([string[]]$Names)

    foreach ($name in $Names) {
        $command = Get-Command $name -ErrorAction SilentlyContinue
        if ($command) {
            return $command.Source
        }
    }

    throw "Required command not found: $($Names -join ', ')"
}

function Invoke-DockerCompose {
    param([string[]]$Arguments)

    try {
        & docker compose version *> $null
        & docker compose @Arguments
        return
    }
    catch {
        $dockerCompose = Get-Command docker-compose -ErrorAction SilentlyContinue
        if (-not $dockerCompose) {
            throw "Docker Compose is required but was not found."
        }
        & $dockerCompose.Source @Arguments
    }
}

function Start-TrackedProcess {
    param(
        [string]$Name,
        [string]$FilePath,
        [string[]]$ArgumentList,
        [string]$WorkingDirectory,
        [string]$LogPrefix
    )

    $stdout = Join-Path $logDir "$LogPrefix.log"
    $stderr = Join-Path $logDir "$LogPrefix.err.log"
    Write-Host "Starting $Name; logs: $stdout"

    $process = Start-Process `
        -WindowStyle Hidden `
        -FilePath $FilePath `
        -ArgumentList $ArgumentList `
        -WorkingDirectory $WorkingDirectory `
        -RedirectStandardOutput $stdout `
        -RedirectStandardError $stderr `
        -PassThru

    $processes.Add($process)
}

function Start-BackendService {
    param(
        [string]$Module,
        [string[]]$ExtraArgs = @()
    )

    $quotedExtraArgs = $ExtraArgs | ForEach-Object { "'$_'" }
    $command = "mvn -pl $Module spring-boot:run"
    if ($quotedExtraArgs.Count -gt 0) {
        $command = "$command $($quotedExtraArgs -join ' ')"
    }

    Start-TrackedProcess `
        -Name $Module `
        -FilePath (Resolve-Tool @("powershell.exe", "powershell")) `
        -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", $command) `
        -WorkingDirectory $backendDir `
        -LogPrefix $Module
    Start-Sleep -Seconds $backendStartDelay
}

$gatewayArgs = @(
    "--spring.cloud.gateway.routes[0].id=auth-service",
    "--spring.cloud.gateway.routes[0].uri=http://localhost:8101",
    "--spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**",
    "--spring.cloud.gateway.routes[1].id=metadata-service",
    "--spring.cloud.gateway.routes[1].uri=http://localhost:8102",
    "--spring.cloud.gateway.routes[1].predicates[0]=Path=/api/metadata/**",
    "--spring.cloud.gateway.routes[2].id=ai-service",
    "--spring.cloud.gateway.routes[2].uri=http://localhost:8104",
    "--spring.cloud.gateway.routes[2].predicates[0]=Path=/api/ai/**",
    "--spring.cloud.gateway.routes[3].id=harness-compiler",
    "--spring.cloud.gateway.routes[3].uri=http://localhost:8090",
    "--spring.cloud.gateway.routes[3].predicates[0]=Path=/api/compiler/**",
    "--spring.cloud.gateway.routes[4].id=agent-adapter",
    "--spring.cloud.gateway.routes[4].uri=http://localhost:8088",
    "--spring.cloud.gateway.routes[4].predicates[0]=Path=/api/agent-adapter/**",
    "--spring.cloud.gateway.routes[5].id=orchestrator-service",
    "--spring.cloud.gateway.routes[5].uri=http://localhost:8091",
    "--spring.cloud.gateway.routes[5].predicates[0]=Path=/api/orchestrator/**",
    "--spring.cloud.gateway.routes[6].id=graph-runtime",
    "--spring.cloud.gateway.routes[6].uri=http://localhost:8091",
    "--spring.cloud.gateway.routes[6].predicates[0]=Path=/api/graph/**"
)

try {
    Write-Host "Starting base services with Docker Compose..."
    Invoke-DockerCompose @("-f", (Join-Path $backendDir "compose.yaml"), "up", "-d")

    if ($env:EIP_SKIP_BACKEND_BUILD -ne "1") {
        Write-Host "Building backend modules with tests skipped..."
        Push-Location $backendDir
        try {
            & (Resolve-Tool @("mvn.cmd", "mvn")) -DskipTests install
        }
        finally {
            Pop-Location
        }
    }

    Start-BackendService "gateway-service" @("-Dspring-boot.run.arguments=$($gatewayArgs -join ' ')")
    Start-BackendService "auth-service"
    Start-BackendService "metadata-service"
    Start-BackendService "ai-service"
    Start-BackendService "harness-compiler"
    Start-BackendService "agent-adapter"
    Start-BackendService "orchestrator-service"

    if (-not (Test-Path (Join-Path $frontendDir "node_modules"))) {
        Write-Host "Installing frontend dependencies..."
        Push-Location $frontendDir
        try {
            & (Resolve-Tool @("npm.cmd", "npm")) install
        }
        finally {
            Pop-Location
        }
    }

    Start-TrackedProcess `
        -Name "frontend" `
        -FilePath (Resolve-Tool @("npm.cmd", "npm")) `
        -ArgumentList @("run", "dev", "--", "--host", "127.0.0.1", "--port", $frontendPort, "--strictPort") `
        -WorkingDirectory $frontendDir `
        -LogPrefix "frontend"

    Write-Host ""
    Write-Host "Development environment is starting:"
    Write-Host "  Frontend             http://127.0.0.1:$frontendPort"
    Write-Host "  Gateway API          http://localhost:8080"
    Write-Host "  Orchestrator API     http://localhost:8091"
    Write-Host "  Qdrant               http://localhost:6333"
    Write-Host "  Ollama               http://localhost:11434"
    Write-Host ""
    Write-Host "Logs: $logDir"
    Write-Host "Login: admin / admin"
    Write-Host ""
    Write-Host "Press Ctrl+C to stop frontend/backend processes. Docker services remain running."
    Write-Host "Stop Docker services with: docker compose -f backend/compose.yaml down"

    while ($true) {
        Start-Sleep -Seconds 2
        foreach ($process in $processes) {
            if ($process.HasExited) {
                throw "Process $($process.Id) exited. Check logs in $logDir."
            }
        }
    }
}
finally {
    foreach ($process in $processes) {
        if (-not $process.HasExited) {
            Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
        }
    }
}
