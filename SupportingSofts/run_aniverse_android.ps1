$ErrorActionPreference = "Stop"

. "$PSScriptRoot\setup_env.ps1"

$ApkPath = "F:\cool projects\Aniverse\android\app\build\outputs\apk\debug\app-debug.apk"

if (!(Test-Path $ApkPath)) {
    Write-Host "APK not found. Building first..." -ForegroundColor Yellow
    & "$PSScriptRoot\build_aniverse_android.ps1"
}

Write-Host "`n=== Checking Android Devices / Emulator ===" -ForegroundColor Cyan
$devices = & adb devices | Out-String

if ($devices -match "emulator-\d+\s+device" -or $devices -match "\w+\s+device") {
    Write-Host "Target device/emulator is online and ready." -ForegroundColor Green
} else {
    Write-Host "Starting emulator 'aniverse_avd'..." -ForegroundColor Yellow
    Start-Process -FilePath "emulator" -ArgumentList "-avd aniverse_avd -no-snapshot-load"
    Write-Host "Waiting for device to connect..." -ForegroundColor Yellow
    & adb wait-for-device
    
    Write-Host "Waiting for Android system boot completion..." -ForegroundColor Yellow
    $booted = $false
    for ($i = 0; $i -lt 30; $i++) {
        $bootProp = & adb shell getprop sys.boot_completed 2>$null
        if ($bootProp -match "1") {
            $booted = $true
            break
        }
        Start-Sleep -Seconds 3
    }
}

Write-Host "`nInstalling Aniverse App APK..." -ForegroundColor Cyan
& adb install -r $ApkPath

Write-Host "Launching Aniverse App..." -ForegroundColor Green
& adb shell am start -n com.aniverse.app/.MainActivity

Write-Host "`n[DONE] Aniverse is running on the Android device/emulator!" -ForegroundColor Green
