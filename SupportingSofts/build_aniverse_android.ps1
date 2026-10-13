$ErrorActionPreference = "Stop"

. "$PSScriptRoot\setup_env.ps1"

$ProjectDir = "F:\cool projects\Aniverse\android"
Set-Location $ProjectDir

Write-Host "`n=== Building Aniverse Android App (Optimized Release) ===" -ForegroundColor Cyan
Write-Host "Project Directory: $ProjectDir" -ForegroundColor Cyan
Write-Host "Gradle User Home:  $env:GRADLE_USER_HOME" -ForegroundColor Cyan
Write-Host "Android Home:      $env:ANDROID_HOME" -ForegroundColor Cyan

# Run gradle assembleRelease
& "$PSScriptRoot\gradle-8.7\bin\gradle.bat" :app:assembleRelease --stacktrace

$ReleaseDir = "$ProjectDir\app\build\outputs\apk\release"
if (Test-Path $ReleaseDir) {
    Write-Host "`n[SUCCESS] Aniverse Release APK(s) built successfully!" -ForegroundColor Green
    $distDir = "F:\cool projects\Aniverse\release\android"
    if (-not (Test-Path $distDir)) {
        New-Item -ItemType Directory -Path $distDir -Force | Out-Null
    }

    Get-ChildItem -Path $ReleaseDir -Filter "*.apk" | ForEach-Object {
        $sizeMB = [math]::Round($_.Length / 1MB, 2)
        Write-Host "  -> $($_.Name) ($sizeMB MB) at $($_.FullName)" -ForegroundColor Green

        if ($_.Name -like "*arm64*") {
            Copy-Item $_.FullName (Join-Path $distDir "Aniverse-v1.0.0-arm64.apk") -Force
        } elseif ($_.Name -like "*v7a*") {
            Copy-Item $_.FullName (Join-Path $distDir "Aniverse-v1.0.0-armv7.apk") -Force
        }
    }
    Write-Host "`n[COPIED] Release APKs updated in $distDir" -ForegroundColor Cyan
} else {
    Write-Host "`n[ERROR] Build finished but release directory was not found at $ReleaseDir" -ForegroundColor Red
}
