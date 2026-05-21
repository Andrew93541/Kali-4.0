Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  KALI - WOMEN'S SAFETY SYSTEM LOCAL STARTUP" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Starting Backend API Server on port 3000..." -ForegroundColor Green
Start-Process cmd.exe -ArgumentList "/c cd /d `"$PSScriptRoot\Kali-Backend`" && npm start" -Title "KALI Backend API"

Write-Host "Starting Web Portal on port 8080..." -ForegroundColor Green
Start-Process cmd.exe -ArgumentList "/c cd /d `"$PSScriptRoot\Kali-Web`" && npx http-server -p 8080" -Title "KALI Web Frontend"

Write-Host ""
Write-Host "Launching default web browser to http://localhost:8080/login.html..." -ForegroundColor Yellow
Start-Sleep -Seconds 3
Start-Process "http://localhost:8080/login.html"

Write-Host ""
Write-Host "---------------------------------------------------" -ForegroundColor Gray
Write-Host "  Android Client App Instructions:" -ForegroundColor Gray
Write-Host "---------------------------------------------------" -ForegroundColor Gray
Write-Host "To run the Kotlin Android app on your emulator or device:"
Write-Host "1. Open Android Studio."
Write-Host "2. Open the project folder: $PSScriptRoot\Kali-Android"
Write-Host "3. Run the 'app' module onto your emulator/device."
Write-Host ""
Write-Host "Both servers are running in separate command windows." -ForegroundColor Green
Write-Host "===================================================" -ForegroundColor Cyan
