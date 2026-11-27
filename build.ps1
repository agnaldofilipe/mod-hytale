$ErrorActionPreference = "Stop"

$jdkDir = (Get-ChildItem -Path "C:\Program Files\Eclipse Adoptium" -Directory | Where-Object { $_.Name -like "jdk-25*" } | Select-Object -First 1).FullName
if (-not $jdkDir) {
    Write-Error "JDK 25 nao encontrado em C:\Program Files\Eclipse Adoptium"
}

$javac = Join-Path $jdkDir "bin\javac.exe"
$jar = Join-Path $jdkDir "bin\jar.exe"

$hytaleServerJar = Join-Path $env:APPDATA "Hytale\install\release\package\game\latest\Server\HytaleServer.jar"
if (-not (Test-Path $hytaleServerJar)) {
    Write-Error "HytaleServer.jar nao encontrado em $hytaleServerJar"
}

$projectRoot = $PSScriptRoot
$srcJavaDir = Join-Path $projectRoot "src\main\java"
$srcResDir = Join-Path $projectRoot "src\main\resources"
$buildDir = Join-Path $projectRoot "build"
$classesDir = Join-Path $buildDir "classes"
$libsDir = Join-Path $buildDir "libs"
$outputJar = Join-Path $libsDir "HytaleIndustrialRevolution-0.0.1-TEST.jar"

Write-Host "=== Compilando HIR (Hytale Industrial Revolution) ===" -ForegroundColor Cyan
Write-Host "JDK: $jdkDir"
Write-Host "HytaleServer: $hytaleServerJar"

if (Test-Path $classesDir) { Remove-Item -Path $classesDir -Recurse -Force }
New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
New-Item -ItemType Directory -Path $libsDir -Force | Out-Null

$javaFiles = Get-ChildItem -Path $srcJavaDir -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
if ($javaFiles.Count -eq 0) {
    Write-Error "Nenhum arquivo Java encontrado em $srcJavaDir"
}

& $javac -cp $hytaleServerJar -d $classesDir --release 25 $javaFiles
if ($LASTEXITCODE -ne 0) {
    Write-Error "Falha na compilacao Java"
}

Write-Host "Copiando recursos e manifest.json..." -ForegroundColor Green
Copy-Item -Path "$srcResDir\*" -Destination $classesDir -Recurse -Force

Write-Host "Empacotando JAR: $outputJar..." -ForegroundColor Green
& $jar --create --file $outputJar -C $classesDir .
if ($LASTEXITCODE -ne 0) {
    Write-Error "Falha ao gerar o arquivo JAR"
}

Write-Host "=== Build concluido com sucesso! ===" -ForegroundColor Green
Write-Host "JAR gerado em: $outputJar"
