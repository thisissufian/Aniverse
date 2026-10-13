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
    Get-ChildItem -Path $ReleaseDir -Filter "*.apk" | ForEach-Object {
        $sizeMB = [math]::Round($_.Length / 1MB, 2)
        Write-Host "  -> $($_.Name) ($sizeMB MB) at $($_.FullName)" -ForegroundColor Green
    }
} else {
    Write-Host "`n[ERROR] Build finished but release directory was not found at $ReleaseDir" -ForegroundColor Red
}
