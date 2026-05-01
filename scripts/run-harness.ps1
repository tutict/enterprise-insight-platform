Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $repoRoot "backend"

Push-Location $backendDir
try {
    mvn -pl harness-compiler-platform -am spring-boot:run
}
finally {
    Pop-Location
}
