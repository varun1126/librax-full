@echo off
title LibraX - Library Management Platform
cd /d "%~dp0"

echo ====================================================================
echo   📚 LibraX - Intelligent Library Management Platform
echo ====================================================================
echo   Starting Backend & Frontend on Port 8085...
echo   Opening browser at http://localhost:8085 in 5 seconds...
echo.
echo   * Keep this terminal open while using LibraX.
echo   * Press Ctrl + C to stop the application.
echo ====================================================================
echo.

:: Wait 5 seconds for Spring Boot to initialize, then launch the browser
start "" cmd /c "timeout /t 5 /nobreak >nul & start http://localhost:8085"

:: Start the application
mvn spring-boot:run

pause
