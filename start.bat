@echo off
title KALI – Startup Orchestrator
echo ===================================================
echo   KALI - WOMEN'S SAFETY SYSTEM LOCAL STARTUP
echo ===================================================
echo.
echo Starting Backend API Server on port 3000...
start "KALI Backend API" cmd /c "cd /d "%~dp0Kali-Backend" && npm start"

echo.
echo Starting Web Portal on port 8080...
start "KALI Web Frontend" cmd /c "cd /d "%~dp0Kali-Web" && npx http-server -p 8080"

echo.
echo Launching default web browser to http://localhost:8080/login.html...
timeout /t 3 >nul
start http://localhost:8080/login.html

echo.
echo ---------------------------------------------------
echo   Android Client App Instructions:
echo ---------------------------------------------------
echo To run the Kotlin Android app on your emulator or device:
echo 1. Open Android Studio.
echo 2. Open the project folder: %~dp0Kali-Android
echo 3. Run the 'app' module onto your emulator/device.
echo.
echo ---------------------------------------------------
echo Both servers are now running in separate command windows.
echo Close their command windows to stop the servers.
echo ===================================================
pause
