@echo off
REM DBAssist Launcher
REM Checks for Java Runtime Environment before launching

setlocal

REM Application Info
set APP_NAME=DBAssist
set APP_VERSION=1.0.0
set REQUIRED_JAVA_VERSION=17

echo Starting %APP_NAME% v%APP_VERSION%...
echo.

REM Check if JAVA_HOME is set
if not "%JAVA_HOME%"=="" (
    set "JAVA_EXEC=%JAVA_HOME%\bin\java.exe"
    goto :check_java
)

REM Check if java is in PATH
where java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set "JAVA_EXEC=java"
    goto :check_java
)

REM Java not found
goto :java_not_found

:check_java
REM Check Java version
echo Checking Java installation...
"%JAVA_EXEC%" -version 2>&1 | find "version" >nul
if %ERRORLEVEL% NEQ 0 (
    goto :java_not_found
)

REM Get Java version
for /f "tokens=3" %%g in ('"%JAVA_EXEC%" -version 2^>^&1 ^| find "version"') do (
    set JAVA_VERSION=%%g
)
set JAVA_VERSION=%JAVA_VERSION:"=%

REM Extract major version
for /f "tokens=1 delims=." %%a in ("%JAVA_VERSION%") do set JAVA_MAJOR=%%a

REM Handle Java 9+ version format
if %JAVA_MAJOR% GEQ %REQUIRED_JAVA_VERSION% (
    goto :launch_app
)

REM Handle Java 1.8 format
for /f "tokens=1,2 delims=." %%a in ("%JAVA_VERSION%") do (
    if "%%a"=="1" (
        if %%b GEQ %REQUIRED_JAVA_VERSION% (
            goto :launch_app
        )
    )
)

REM Java version too old
goto :java_version_error

:launch_app
echo Java found: %JAVA_VERSION%
echo Launching %APP_NAME%...
echo.

REM Get the directory where this script is located
set "APP_DIR=%~dp0"

REM Launch the application
"%JAVA_EXEC%" -jar "%APP_DIR%DBAssist.jar"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Application failed to start
    echo Error code: %ERRORLEVEL%
    echo.
    pause
)
goto :end

:java_not_found
cls
echo ========================================
echo   ERROR: Java Not Found
echo ========================================
echo.
echo %APP_NAME% requires Java Runtime Environment (JRE) %REQUIRED_JAVA_VERSION% or higher.
echo.
echo Java is not installed or not in your system PATH.
echo.
echo Please follow these steps:
echo.
echo 1. Download JRE %REQUIRED_JAVA_VERSION% or higher from:
echo    https://www.oracle.com/java/technologies/downloads/
echo    or
echo    https://adoptium.net/
echo.
echo 2. Install Java Runtime Environment
echo.
echo 3. Set JAVA_HOME environment variable:
echo    - Right-click 'This PC' ^> Properties
echo    - Click 'Advanced system settings'
echo    - Click 'Environment Variables'
echo    - Add new System Variable:
echo      Variable name: JAVA_HOME
echo      Variable value: C:\Program Files\Java\jdk-17
echo.
echo 4. Add Java to PATH:
echo    - In Environment Variables
echo    - Edit 'Path' variable
echo    - Add: %%JAVA_HOME%%\bin
echo.
echo 5. Restart this application
echo.
echo ========================================
pause
goto :end

:java_version_error
cls
echo ========================================
echo   ERROR: Java Version Too Old
echo ========================================
echo.
echo %APP_NAME% requires Java %REQUIRED_JAVA_VERSION% or higher.
echo Your current Java version: %JAVA_VERSION%
echo.
echo Please upgrade your Java installation:
echo.
echo 1. Download JRE %REQUIRED_JAVA_VERSION% or higher from:
echo    https://www.oracle.com/java/technologies/downloads/
echo    or
echo    https://adoptium.net/
echo.
echo 2. Install the new Java version
echo.
echo 3. Update JAVA_HOME environment variable
echo.
echo 4. Restart this application
echo.
echo ========================================
pause
goto :end

:end
endlocal

