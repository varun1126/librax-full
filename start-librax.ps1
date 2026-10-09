# LibraX Launcher PowerShell Script
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  Launching LibraX Library Management Platform...       " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Starting Spring Boot Backend (Port 8085)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run"

Write-Host "Starting React + Vite Frontend (Port 5173)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd frontend; npm run dev"

Write-Host ""
Write-Host "========================================================" -ForegroundColor Green
Write-Host "  Services are starting up!                             " -ForegroundColor Green
Write-Host "  Frontend URL : http://localhost:5173                  " -ForegroundColor Green
Write-Host "  Backend API  : http://localhost:8085                  " -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
Write-Host ""
