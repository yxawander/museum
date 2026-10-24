@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo ============================================
echo   Museum Reservation System Launcher
echo ============================================
echo.

where java >nul 2>nul
if errorlevel 1 goto no_java

where node >nul 2>nul
if errorlevel 1 goto no_node

where npx >nul 2>nul
if errorlevel 1 goto no_node

echo [CHECK] MySQL 3306 ...
powershell -NoProfile -Command "if (-not (Test-NetConnection -ComputerName 127.0.0.1 -Port 3306 -InformationLevel Quiet -WarningAction SilentlyContinue)) { exit 1 }"
if errorlevel 1 goto no_mysql

if exist "%~dp0backend\booking\target\booking-1.0.0.jar" goto check_backend

echo [BUILD] Backend jar not found, building ...
pushd "%~dp0backend\booking"
call mvn -DskipTests package
if errorlevel 1 (
    popd
    goto build_failed
)
popd

:check_backend
set "BACKEND_RUNNING=0"
powershell -NoProfile -Command "if (Test-NetConnection -ComputerName 127.0.0.1 -Port 8080 -InformationLevel Quiet -WarningAction SilentlyContinue) { exit 0 } else { exit 1 }"
if not errorlevel 1 set "BACKEND_RUNNING=1"

if "%BACKEND_RUNNING%"=="1" (
    echo [INFO] Backend already running on 8080.
) else (
    echo [START] Backend ...
    start "Museum Backend" /D "%~dp0backend\booking" cmd /k "java -jar target\booking-1.0.0.jar"
)

if not exist "%~dp0frontend\node_modules" goto install_frontend
goto check_frontend

:install_frontend
echo [INSTALL] Frontend dependencies not found, running npm install ...
pushd "%~dp0frontend"
call npm install
if errorlevel 1 (
    popd
    goto install_failed
)
popd

:check_frontend
set "FRONTEND_RUNNING=0"
powershell -NoProfile -Command "if (Test-NetConnection -ComputerName 127.0.0.1 -Port 5173 -InformationLevel Quiet -WarningAction SilentlyContinue) { exit 0 } else { exit 1 }"
if not errorlevel 1 set "FRONTEND_RUNNING=1"

if "%FRONTEND_RUNNING%"=="1" (
    echo [INFO] Frontend already running on 5173.
) else (
    echo [START] Frontend ...
    start "Museum Frontend" /D "%~dp0frontend" cmd /k "npx vite --host 127.0.0.1 --port 5173"
)

echo.
echo [WAIT] Waiting for backend and frontend ...
powershell -NoProfile -Command "$deadline=(Get-Date).AddSeconds(60); while((Get-Date) -lt $deadline){ $b=(Test-NetConnection -ComputerName 127.0.0.1 -Port 8080 -InformationLevel Quiet -WarningAction SilentlyContinue); $f=(Test-NetConnection -ComputerName 127.0.0.1 -Port 5173 -InformationLevel Quiet -WarningAction SilentlyContinue); if($b -and $f){ exit 0 }; Start-Sleep -Seconds 2 }; exit 1"

if errorlevel 1 (
    echo [WARN] Services are taking longer than expected.
    echo        Check the Backend / Frontend windows for errors.
) else (
    echo [OK] Services are ready.
)

echo.
echo Frontend : http://127.0.0.1:5173
echo Backend  : http://127.0.0.1:8080
echo.
echo Close the "Museum Backend" and "Museum Frontend" windows to stop.
echo.

start "" "http://127.0.0.1:5173"
timeout /t 3 >nul
exit /b 0

:no_java
echo [ERROR] Java not found.
echo         Install JDK 21 and make sure "java" is in PATH.
pause
exit /b 1

:no_node
echo [ERROR] Node.js / npx not found.
echo         Install Node.js 22+ and make sure "node" and "npx" are in PATH.
pause
exit /b 1

:no_mysql
echo [ERROR] MySQL 3306 is not reachable.
echo         Start your MySQL service first, then run this file again.
pause
exit /b 1

:build_failed
echo [ERROR] Backend build failed.
echo         Open backend\booking and run: mvn package
pause
exit /b 1

:install_failed
echo [ERROR] npm install failed.
echo         Open frontend and run: npm install
pause
exit /b 1