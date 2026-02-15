@echo off
REM Quick Build Script - Creates JAR with dependencies
REM For full installer, use build-installer.bat

echo ========================================
echo DBAssist Quick Build
echo ========================================
echo.

echo Building JAR with Maven...
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Build failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.
echo Output: target\DBAssist.jar
echo.
echo To run:
echo 1. Copy target\DBAssist.jar to deployment folder
echo 2. Copy DBAssist-Launcher.bat to same folder
echo 3. Run DBAssist-Launcher.bat
echo.
echo Or run directly:
echo    java -jar target\DBAssist.jar
echo.
pause

