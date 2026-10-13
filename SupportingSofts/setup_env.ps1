$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

$env:JAVA_HOME = "$ScriptDir\AndroidSDK\openjdk"
$env:ANDROID_HOME = "$ScriptDir\AndroidSDK\android-sdk"
$env:ANDROID_SDK_ROOT = "$ScriptDir\AndroidSDK\android-sdk"
$env:GRADLE_USER_HOME = "$ScriptDir\.gradle"
$env:ANDROID_USER_HOME = "$ScriptDir\.android"
$env:ANDROID_AVD_HOME = "$ScriptDir\.android\avd"

$binPaths = @(
    "$env:JAVA_HOME\bin",
    "$env:ANDROID_HOME\cmdline-tools\latest\bin",
    "$env:ANDROID_HOME\platform-tools",
    "$env:ANDROID_HOME\emulator",
    "$ScriptDir\gradle-8.7\bin"
)

foreach ($p in $binPaths) {
    if ($env:PATH -notlike "*$p*") {
        $env:PATH = "$p;$env:PATH"
    }
}

Write-Host "Environment configured successfully:" -ForegroundColor Green
Write-Host "  JAVA_HOME:        $env:JAVA_HOME" -ForegroundColor Cyan
Write-Host "  ANDROID_HOME:     $env:ANDROID_HOME" -ForegroundColor Cyan
Write-Host "  GRADLE_USER_HOME: $env:GRADLE_USER_HOME" -ForegroundColor Cyan
Write-Host "  ANDROID_USER_HOME:$env:ANDROID_USER_HOME" -ForegroundColor Cyan
