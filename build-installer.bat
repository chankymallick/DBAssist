@echo off
REM DBAssist Windows Installer Build Script
REM This script creates a Windows installer using jpackage

echo ========================================
echo DBAssist Production Build
echo ========================================
echo.

REM Check if JAVA_HOME is set
if "%JAVA_HOME%"=="" (
    echo ERROR: JAVA_HOME is not set
    echo Please set JAVA_HOME to your JDK 17+ installation
    pause
    exit /b 1
)

echo JAVA_HOME: %JAVA_HOME%
echo.

REM Check Java version
echo Checking Java version...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java not found or not in PATH
    pause
    exit /b 1
)
echo.

REM Step 1: Clean and build the project
echo Step 1: Building Maven project...
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven build failed
    pause
    exit /b 1
)
echo Build completed successfully!
echo.

REM Step 2: Create output directory
echo Step 2: Preparing installer directory...
if not exist "installer" mkdir installer
if not exist "installer\input" mkdir installer\input
if not exist "installer\output" mkdir installer\output

REM Copy the JAR file
copy target\DBAssist.jar installer\input\DBAssist.jar
echo.

REM Step 3: Create jpackage installer
echo Step 3: Creating Windows installer with jpackage...
echo This may take several minutes...
echo.

jpackage ^
    --type exe ^
    --name "DBAssist" ^
    --app-version "1.0.0" ^
    --vendor "Masaddat Mallick" ^
    --description "AI-Powered Database Assistant for SQL Server and Oracle" ^
    --copyright "Copyright 2026 Masaddat Mallick" ^
    --input installer\input ^
    --main-jar DBAssist.jar ^
    --main-class com.dbassist.dbassist.Launcher ^
    --dest installer\output ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut ^
    --win-per-user-install ^
    --java-options "-Xmx1024m" ^
    --java-options "-Xms256m"

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: jpackage failed
    echo Make sure you have JDK 17+ with jpackage tool
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.
echo Installer created at: installer\output\DBAssist-1.0.0.exe
echo.
echo You can now distribute this installer to users.
echo The installer will check for Java Runtime Environment.
echo.
pause

