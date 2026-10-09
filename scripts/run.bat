@echo off
cd ..
title LibraX Enterprise Library System
echo ====================================================
echo        Starting LibraX Enterprise Library System
echo ====================================================
echo.
echo URL:           http://localhost:8085
echo Admin Login:   admin / Admin@123
echo Student Login: student01 / Student@123
echo.
echo ====================================================
echo Starting Spring Boot...
mvn spring-boot:run
pause
