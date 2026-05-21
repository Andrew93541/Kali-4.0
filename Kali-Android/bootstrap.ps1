# bootstrap.ps1 — Run this once before opening in Android Studio
# Downloads gradle-wrapper.jar so gradlew works

$wrapperDir = "$PSScriptRoot\gradle\wrapper"
$jarPath    = "$wrapperDir\gradle-wrapper.jar"
$jarUrl     = "https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar"

if (-Not (Test-Path $jarPath)) {
    Write-Host "Downloading gradle-wrapper.jar..."
    Invoke-WebRequest -Uri $jarUrl -OutFile $jarPath
    Write-Host "Done. You can now open the project in Android Studio."
} else {
    Write-Host "gradle-wrapper.jar already present."
}
