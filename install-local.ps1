$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
& (Join-Path $projectRoot "build.ps1")

$outputJar = Join-Path $projectRoot "build\libs\HytaleIndustrialRevolution-0.0.1-TEST.jar"
$modsDir = Join-Path $env:APPDATA "Hytale\UserData\Mods"

if (-not (Test-Path $modsDir)) {
    New-Item -ItemType Directory -Path $modsDir -Force | Out-Null
}

$destFile = Join-Path $modsDir "HytaleIndustrialRevolution-0.0.1-TEST.jar"
Copy-Item -Path $outputJar -Destination $destFile -Force

Write-Host "=== Mod instalado localmente no Hytale! ===" -ForegroundColor Green
Write-Host "Destino: $destFile"
