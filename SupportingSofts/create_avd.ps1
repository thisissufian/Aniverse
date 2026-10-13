. "$PSScriptRoot\setup_env.ps1"

Write-Host "Creating Android Virtual Device 'aniverse_avd'..." -ForegroundColor Cyan

# Create the AVD directory if needed
$avdDir = "$env:ANDROID_AVD_HOME"
if (!(Test-Path $avdDir)) {
    New-Item -ItemType Directory -Path $avdDir -Force | Out-Null
}

cmd /c "echo no | ""$env:ANDROID_HOME\cmdline-tools\latest\bin\avdmanager.bat"" create avd -n aniverse_avd -k ""system-images;android-34;google_apis;x86_64"" --device ""pixel_6"" --force"

Write-Host "AVD Creation Complete. Listing AVDs:" -ForegroundColor Green
& "$env:ANDROID_HOME\cmdline-tools\latest\bin\avdmanager.bat" list avd
