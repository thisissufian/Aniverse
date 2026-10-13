$ErrorActionPreference = "Stop"

. "$PSScriptRoot\setup_env.ps1"

$ProjectDir = "F:\cool projects\Aniverse\android"
Set-Location $ProjectDir

Write-Host "`n=== Building Aniverse Android App ===" -ForegroundColor Cyan
Write-Host "Project Directory: $ProjectDir" -ForegroundColor Cyan
Write-Host "Gradle User Home:  $env:GRADLE_USER_HOME" -ForegroundColor Cyan
Write-Host "Android Home:      $env:ANDROID_HOME" -ForegroundColor Cyan

# Run gradle assembleDebug
& "$PSScriptRoot\gradle-8.7\bin\gradle.bat" :app:assembleDebug --stacktrace

$ApkPath = "$ProjectDir\app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $ApkPath) {
    $sizeMB = [math]::Round((Get-Item $ApkPath).Length / 1MB, 2)
    Write-Host "`n[SUCCESS] Aniverse APK built successfully!" -ForegroundColor Green
    Write-Host "APK Location: $ApkPath ($sizeMB MB)" -ForegroundColor Green
} else {
    Write-Host "`n[ERROR] Build finished but APK was not found at $ApkPath" -ForegroundColor Red
}
