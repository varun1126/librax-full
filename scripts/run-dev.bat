@echo off
cd ..
title LibraX Dual Dev Mode Launcher
echo Starting LibraX Dual Dev Mode...
start "LibraX Backend (:8085)" cmd /k "mvn spring-boot:run"
start "LibraX Frontend (:5173)" cmd /k "cd frontend && npm run dev"
echo Both servers launched!
echo Access the live app at: http://localhost:5173
pause
