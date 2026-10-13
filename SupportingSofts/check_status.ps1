. "$PSScriptRoot\setup_env.ps1"

Write-Host "`n=== Testing Java ===" -ForegroundColor Yellow
java -version

Write-Host "`n=== Testing ADB ===" -ForegroundColor Yellow
adb version

Write-Host "`n=== Testing SDK Manager ===" -ForegroundColor Yellow
sdkmanager --list_installed

Write-Host "`n=== Listing AVDs ===" -ForegroundColor Yellow
avdmanager list avd
