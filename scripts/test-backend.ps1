Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $repoRoot "backend"

Push-Location $backendDir
try {
    mvn test
}
finally {
    Pop-Location
}
