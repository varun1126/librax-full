# LibraX Dual Dev Mode Launcher
$root = Resolve-Path "$PSScriptRoot\.."
Set-Location $root

Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "       Starting LibraX Dual Dev Mode Launcher        " -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Frontend URL:  http://localhost:5173" -ForegroundColor Green
Write-Host "Backend API:   http://localhost:8085" -ForegroundColor Green
Write-Host ""

Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$root'; mvn spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$root\frontend'; npm run dev"
