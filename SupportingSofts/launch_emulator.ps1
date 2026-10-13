. "$PSScriptRoot\setup_env.ps1"

Write-Host "Starting Android Emulator 'aniverse_avd'..." -ForegroundColor Cyan
& "$env:ANDROID_HOME\emulator\emulator.exe" -avd aniverse_avd -gpu host
