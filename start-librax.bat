@echo off
title LibraX Launcher
echo ========================================================
echo   Launching LibraX Library Management Platform...
echo ========================================================
echo.
echo Starting Spring Boot Backend (Port 8085)...
start "LibraX Backend [Port 8085]" cmd /k "mvn spring-boot:run"

echo Starting React + Vite Frontend (Port 5173)...
start "LibraX Frontend [Port 5173]" cmd /k "cd frontend && npm run dev"

echo.
echo ========================================================
echo   Services are starting up!
echo   Frontend URL : http://localhost:5173
echo   Backend API  : http://localhost:8085
echo ========================================================
echo.
pause
