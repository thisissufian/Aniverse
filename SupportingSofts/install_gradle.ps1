$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$zipPath = "$ScriptDir\gradle-8.7-bin.zip"
$extractPath = "$ScriptDir"
$gradleBin = "$ScriptDir\gradle-8.7\bin\gradle.bat"

if (!(Test-Path $gradleBin)) {
    if (!(Test-Path $zipPath) -or ((Get-Item $zipPath).Length -lt 100000000)) {
        Write-Host "Downloading Gradle 8.7 to $zipPath..." -ForegroundColor Cyan
        & curl.exe -L -o "$zipPath" "https://services.gradle.org/distributions/gradle-8.7-bin.zip"
    } else {
        Write-Host "Gradle zip already exists: $zipPath" -ForegroundColor Green
    }

    Write-Host "Extracting Gradle 8.7 to $extractPath..." -ForegroundColor Cyan
    Expand-Archive -Path "$zipPath" -DestinationPath "$extractPath" -Force
    Write-Host "Gradle 8.7 extracted." -ForegroundColor Green
} else {
    Write-Host "Gradle 8.7 is already installed at $gradleBin" -ForegroundColor Green
}

Write-Host "Verifying Gradle installation..." -ForegroundColor Yellow
& "$gradleBin" -v
