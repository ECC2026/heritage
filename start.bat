@echo off
setlocal enabledelayedexpansion
title Heritage Local Startup Script

rem ================== CONFIG (edit as needed) ==================
set "ROOT=%~dp0"
set "DB_HOST=localhost"
set "DB_PORT=3306"
set "DB_NAME=heritage"
set "DB_USER=root"
set "DB_PASSWORD=1234"
set "REDIS_DIR=D:\install_path\Redis-x64-5.0.14.1"
set "REDIS_PORT=6379"

rem JWT secret: keep an existing value if set, otherwise use the dev default (>= 32 chars)
if not defined JWT_SECRET set "JWT_SECRET=heritage-local-dev-jwt-secret-20260812-change-me"

set "CMD=%~1"
if "%CMD%"=="" set "CMD=all"

echo.
echo ================================================
echo   Heritage Local Startup Script
echo   Mode: %CMD%
echo   Usage: start.bat [all^|init^|deps^|backend^|manage^|client]
echo ================================================
echo.

if /i "%CMD%"=="init"    goto init_db
if /i "%CMD%"=="deps"    goto deps
if /i "%CMD%"=="backend" goto start_backend
if /i "%CMD%"=="manage"  goto start_manage
if /i "%CMD%"=="client"  goto start_client
if /i not "%CMD%"=="all" (
  echo Unknown mode: %CMD%
  goto END
)

rem ================== all: full startup ==================
call :check_mysql
if "!MYSQL_OK!"=="0" (
  echo Please start MySQL (Windows service name: MySQL) first, then run again.
  goto END
)
call :ensure_redis
call :ensure_env
call :start_backend
call :start_manage
call :start_client
call :print_urls
goto END

rem ================== init: create DB + apply migrations ==================
:init_db
call :check_mysql
if "!MYSQL_OK!"=="0" goto END

echo [DB] Creating database %DB_NAME% ...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% --default-character-set=utf8mb4 -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
if errorlevel 1 (
  echo [ERROR] Failed to create database.
  goto END
)

rem Baseline check: repo has no V001; if DB is empty, V002 references missing base tables
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% -e "SHOW TABLES LIKE 'category';" | findstr /c:"category" >nul
if errorlevel 1 (
  echo.
  echo [WARN] No baseline table detected (e.g. category). The repo has no ICHIP baseline script (no V001).
  echo If %DB_NAME% is empty, you must import the baseline schema first, otherwise V002 will fail.
  set /p PROCEED="  Still try to apply V002/V003 anyway? (y/N): "
  if /i not "!PROCEED!"=="y" goto END
)

cd /d "%ROOT%"
echo [MIGRATE] Applying V002__phase1_home_foundation.sql ...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% --default-character-set=utf8mb4 %DB_NAME% < "SQL\migrations\V002__phase1_home_foundation.sql"
if errorlevel 1 (
  echo [ERROR] V002 failed, aborting.
  goto END
)
echo [MIGRATE] Applying V003__business_ecosystem.sql ...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% --default-character-set=utf8mb4 %DB_NAME% < "SQL\migrations\V003__business_ecosystem.sql"
if errorlevel 1 (
  echo [ERROR] V003 failed, aborting.
  goto END
)
echo [OK] Migrations applied.

set /p SEED="  Load test data V003/V004? (y/N): "
if /i "!SEED!"=="y" (
  echo [SEED] Applying V003__phase1_home_test_data.sql ...
  mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% --default-character-set=utf8mb4 %DB_NAME% < "SQL\seeds\V003__phase1_home_test_data.sql"
  echo [SEED] Applying V004__business_ecosystem_test_data.sql ...
  mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% --default-character-set=utf8mb4 %DB_NAME% < "SQL\seeds\V004__business_ecosystem_test_data.sql"
)
echo [DONE] Database init finished.
goto END

rem ================== deps: check MySQL + start Redis only ==================
:deps
call :check_mysql
call :ensure_redis
goto END

rem ================== subroutines ==================

:check_mysql
echo [MySQL] Checking MySQL (localhost:%DB_PORT%) ...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% -e "SELECT 1;" >nul 2>&1
if errorlevel 1 (
  echo   [WARN] Cannot connect to MySQL. Check it is running and credentials are correct.
  set "MYSQL_OK=0"
) else (
  echo   [OK] MySQL connected.
  set "MYSQL_OK=1"
)
goto :eof

:ensure_redis
echo [Redis] Checking Redis (port %REDIS_PORT%) ...
set "RCLI="
if exist "%REDIS_DIR%\redis-cli.exe" set "RCLI=%REDIS_DIR%\redis-cli.exe"
if not defined RCLI for /f "delims=" %%i in ('where redis-cli 2^>nul') do if not defined RCLI set "RCLI=%%i"
if not defined RCLI (
  echo   [WARN] redis-cli not found. Install Redis or fix REDIS_DIR at the top of this script.
  goto :eof
)
"!RCLI!" -p %REDIS_PORT% ping >nul 2>&1
if not errorlevel 1 (
  echo   [OK] Redis is running.
  goto :eof
)
if not exist "%REDIS_DIR%\redis-server.exe" (
  echo   [WARN] Redis is not running and redis-server.exe was not found; cannot auto-start it.
  goto :eof
)
echo   Starting Redis ...
start "Heritage-Redis" /d "%REDIS_DIR%" cmd /k "echo [Redis] server window, do not close & redis-server.exe redis.windows.conf"
set /a wait=0
:wait_redis
set /a wait+=1
"!RCLI!" -p %REDIS_PORT% ping >nul 2>&1
if not errorlevel 1 (
  echo   [OK] Redis is ready.
  goto :eof
)
if %wait% geq 15 (
  echo   [WARN] Redis did not become ready within 15s. Check manually.
  goto :eof
)
timeout /t 1 /nobreak >nul
goto wait_redis

:ensure_env
rem manage_code needs a .env on first run
if not exist "%ROOT%manage_code\.env" (
  copy /y "%ROOT%manage_code\.env.example" "%ROOT%manage_code\.env" >nul
  echo [ENV] Generated manage_code\.env
)
rem hint if the database has not been initialized
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% -N -e "SHOW DATABASES;" 2>nul | findstr /i /c:"%DB_NAME%" >nul
if errorlevel 1 (
  echo [HINT] Database %DB_NAME% not found. Run: start.bat init
)
goto :eof

:start_backend
echo [BACKEND] Starting backend (http://localhost:8080/api) ...
start "Heritage-Backend" /d "%ROOT%server_code" cmd /k "set DB_PASSWORD=%DB_PASSWORD% & set JWT_SECRET=%JWT_SECRET% & echo [Backend] Starting, first compile may take a while ... & call mvnw.cmd spring-boot:run"
goto :eof

:start_manage
echo [MANAGE] Starting admin web (http://localhost:5173) ...
if not exist "%ROOT%manage_code\node_modules" (
  echo   First run: installing manage_code dependencies ...
  start "Heritage-Manage" /d "%ROOT%manage_code" cmd /k "echo [Manage] Installing dependencies ... & call npm install & echo. & echo Dependencies installed, starting dev server ... & call npm run dev"
) else (
  start "Heritage-Manage" /d "%ROOT%manage_code" cmd /k "echo [Manage] Starting dev server ... & call npm run dev"
)
goto :eof

:start_client
echo [CLIENT] Starting mini-program build (mp-weixin) ...
if not exist "%ROOT%client_code\node_modules" (
  echo   First run: installing client_code dependencies ...
  start "Heritage-Client" /d "%ROOT%client_code" cmd /k "echo [Client] Installing dependencies ... & call npm install & echo. & call npm run dev:mp-weixin"
) else (
  start "Heritage-Client" /d "%ROOT%client_code" cmd /k "echo [Client] Watch mode for mp-weixin ... & call npm run dev:mp-weixin"
)
goto :eof

:print_urls
echo.
echo ================================================
echo   Services started in separate windows:
echo     Backend  http://localhost:8080/api
echo     Admin    http://localhost:5173
echo     Client   import client_code\unpackage\dist\dev\mp-weixin into WeChat DevTools
echo   Tip: in DevTools enable "do not verify valid domains".
echo ================================================
goto :eof

:END
echo.
pause >nul
endlocal
