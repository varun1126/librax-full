# LibraX Standalone Runner
$root = Resolve-Path "$PSScriptRoot\.."
Set-Location $root

Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "       Starting LibraX Enterprise Library System      " -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "URL:           http://localhost:8085" -ForegroundColor Green
Write-Host "Admin Login:   admin / Admin@123" -ForegroundColor Yellow
Write-Host "Student Login: student01 / Student@123" -ForegroundColor Yellow
Write-Host ""

mvn spring-boot:run
