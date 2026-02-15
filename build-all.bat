@echo off
REM DBAssist - Build All Distribution Packages
REM Creates 3 distribution folders in target:
REM   1. windows-installer   - Windows installer with bundled JRE
REM   2. portable-jar        - Double-click JAR for Windows/Mac/Linux
REM   3. linux-package       - Shell script launcher for Linux

echo ========================================
echo DBAssist - Build All Distributions
echo ========================================
echo.

REM Check prerequisites
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

REM ========================================
REM Step 1: Build the project
REM ========================================
echo ========================================
echo Step 1: Building Maven project...
echo ========================================
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven build failed
    pause
    exit /b 1
)
echo Build completed successfully!
echo.

REM ========================================
REM Step 2: Create distribution folders
REM ========================================
echo ========================================
echo Step 2: Creating distribution folders...
echo ========================================

REM Clean old distributions
if exist "target\windows-installer" rmdir /s /q "target\windows-installer"
if exist "target\portable-jar" rmdir /s /q "target\portable-jar"
if exist "target\linux-package" rmdir /s /q "target\linux-package"

REM Create new distribution folders
mkdir "target\windows-installer"
mkdir "target\portable-jar"
mkdir "target\linux-package"

echo Distribution folders created.
echo.

REM ========================================
REM Step 3: Prepare Portable JAR Distribution
REM ========================================
echo ========================================
echo Step 3: Preparing Portable JAR...
echo ========================================

copy "target\DBAssist.jar" "target\portable-jar\DBAssist.jar"
copy "README.md" "target\portable-jar\README.md"
copy "LICENSE.txt" "target\portable-jar\LICENSE.txt"

REM Create Windows launcher for portable JAR
echo @echo off > "target\portable-jar\DBAssist.bat"
echo REM DBAssist Launcher for Windows >> "target\portable-jar\DBAssist.bat"
echo java -jar DBAssist.jar >> "target\portable-jar\DBAssist.bat"

REM Create Mac/Linux launcher
echo #!/bin/bash > "target\portable-jar\DBAssist.command"
echo # DBAssist Launcher for Mac >> "target\portable-jar\DBAssist.command"
echo java -jar DBAssist.jar >> "target\portable-jar\DBAssist.command"

REM Create README for portable JAR
echo # DBAssist Portable JAR > "target\portable-jar\INSTALL.txt"
echo. >> "target\portable-jar\INSTALL.txt"
echo ## Requirements >> "target\portable-jar\INSTALL.txt"
echo - Java Runtime Environment (JRE) 17 or higher >> "target\portable-jar\INSTALL.txt"
echo - Download from: https://adoptium.net/ >> "target\portable-jar\INSTALL.txt"
echo. >> "target\portable-jar\INSTALL.txt"
echo ## How to Run >> "target\portable-jar\INSTALL.txt"
echo. >> "target\portable-jar\INSTALL.txt"
echo ### Windows: >> "target\portable-jar\INSTALL.txt"
echo Double-click: DBAssist.jar >> "target\portable-jar\INSTALL.txt"
echo Or run: DBAssist.bat >> "target\portable-jar\INSTALL.txt"
echo. >> "target\portable-jar\INSTALL.txt"
echo ### Mac: >> "target\portable-jar\INSTALL.txt"
echo Double-click: DBAssist.command >> "target\portable-jar\INSTALL.txt"
echo (You may need to: chmod +x DBAssist.command) >> "target\portable-jar\INSTALL.txt"
echo. >> "target\portable-jar\INSTALL.txt"
echo ### Linux: >> "target\portable-jar\INSTALL.txt"
echo Run: ./dbassist.sh >> "target\portable-jar\INSTALL.txt"
echo (You may need to: chmod +x dbassist.sh) >> "target\portable-jar\INSTALL.txt"
echo. >> "target\portable-jar\INSTALL.txt"
echo ## Alternative >> "target\portable-jar\INSTALL.txt"
echo Open terminal and run: >> "target\portable-jar\INSTALL.txt"
echo java -jar DBAssist.jar >> "target\portable-jar\INSTALL.txt"

echo Portable JAR prepared: target\portable-jar\
echo.

REM ========================================
REM Step 4: Prepare Linux Package
REM ========================================
echo ========================================
echo Step 4: Preparing Linux Package...
echo ========================================

copy "target\DBAssist.jar" "target\linux-package\DBAssist.jar"
copy "README.md" "target\linux-package\README.md"
copy "LICENSE.txt" "target\linux-package\LICENSE.txt"

REM Create Linux launcher script with JRE detection
(
echo #!/bin/bash
echo # DBAssist Launcher for Linux
echo # Checks for Java Runtime Environment before launching
echo.
echo APP_NAME="DBAssist"
echo APP_VERSION="1.0.0"
echo REQUIRED_JAVA_VERSION=17
echo.
echo echo "Starting $APP_NAME v$APP_VERSION..."
echo echo ""
echo.
echo # Function to check Java version
echo check_java_version^(^) {
echo     if [ -z "$JAVA_HOME" ]; then
echo         # Try to find java in PATH
echo         if command -v java ^&^>/dev/null; then
echo             JAVA_EXEC="java"
echo         else
echo             return 1
echo         fi
echo     else
echo         JAVA_EXEC="$JAVA_HOME/bin/java"
echo     fi
echo.
echo     # Check if java exists
echo     if ! command -v $JAVA_EXEC ^&^>/dev/null; then
echo         return 1
echo     fi
echo.
echo     # Get Java version
echo     JAVA_VERSION=$^($JAVA_EXEC -version 2^>^&1 ^| grep -i version ^| cut -d'"' -f2 ^| cut -d'.' -f1^)
echo.
echo     # Handle Java 1.x format
echo     if [ "$JAVA_VERSION" = "1" ]; then
echo         JAVA_VERSION=$^($JAVA_EXEC -version 2^>^&1 ^| grep -i version ^| cut -d'"' -f2 ^| cut -d'.' -f2^)
echo     fi
echo.
echo     if [ "$JAVA_VERSION" -ge "$REQUIRED_JAVA_VERSION" ]; then
echo         return 0
echo     else
echo         return 2
echo     fi
echo }
echo.
echo # Check Java
echo check_java_version
echo RESULT=$?
echo.
echo if [ $RESULT -eq 1 ]; then
echo     echo "========================================"
echo     echo "  ERROR: Java Not Found"
echo     echo "========================================"
echo     echo ""
echo     echo "$APP_NAME requires Java Runtime Environment ^(JRE^) $REQUIRED_JAVA_VERSION or higher."
echo     echo ""
echo     echo "Java is not installed or not in your PATH."
echo     echo ""
echo     echo "Please install Java:"
echo     echo "1. Ubuntu/Debian: sudo apt install openjdk-17-jre"
echo     echo "2. Fedora/RHEL: sudo dnf install java-17-openjdk"
echo     echo "3. Or download from: https://adoptium.net/"
echo     echo ""
echo     echo "========================================"
echo     exit 1
echo elif [ $RESULT -eq 2 ]; then
echo     echo "========================================"
echo     echo "  ERROR: Java Version Too Old"
echo     echo "========================================"
echo     echo ""
echo     echo "$APP_NAME requires Java $REQUIRED_JAVA_VERSION or higher."
echo     echo ""
echo     echo "Please upgrade Java:"
echo     echo "1. Ubuntu/Debian: sudo apt install openjdk-17-jre"
echo     echo "2. Fedora/RHEL: sudo dnf install java-17-openjdk"
echo     echo "3. Or download from: https://adoptium.net/"
echo     echo ""
echo     echo "========================================"
echo     exit 1
echo fi
echo.
echo echo "Java found: $JAVA_VERSION"
echo echo "Launching $APP_NAME..."
echo echo ""
echo.
echo # Get the directory where this script is located
echo APP_DIR="$^( cd "$^( dirname "${BASH_SOURCE[0]}" ^)" ^&^>/dev/null ^&^& pwd ^)"
echo.
echo # Launch the application
echo cd "$APP_DIR"
echo $JAVA_EXEC -jar DBAssist.jar
echo.
echo # Check exit code
echo if [ $? -ne 0 ]; then
echo     echo ""
echo     echo "ERROR: Application failed to start"
echo     echo "Press Enter to exit..."
echo     read
echo fi
) > "target\linux-package\dbassist.sh"

REM Create install script for Linux
(
echo #!/bin/bash
echo # DBAssist Installation Script for Linux
echo.
echo echo "========================================"
echo echo "DBAssist Installation"
echo echo "========================================"
echo echo ""
echo.
echo # Default installation directory
echo INSTALL_DIR="/opt/dbassist"
echo DESKTOP_FILE="/usr/share/applications/dbassist.desktop"
echo.
echo # Check if running as root for system-wide installation
echo if [ "$EUID" -eq 0 ]; then
echo     echo "Installing DBAssist system-wide..."
echo else
echo     echo "Installing DBAssist for current user..."
echo     INSTALL_DIR="$HOME/.local/share/dbassist"
echo     DESKTOP_FILE="$HOME/.local/share/applications/dbassist.desktop"
echo     mkdir -p "$HOME/.local/share/applications"
echo fi
echo.
echo # Create installation directory
echo echo "Creating installation directory: $INSTALL_DIR"
echo mkdir -p "$INSTALL_DIR"
echo.
echo # Copy files
echo echo "Copying files..."
echo cp DBAssist.jar "$INSTALL_DIR/"
echo cp dbassist.sh "$INSTALL_DIR/"
echo cp README.md "$INSTALL_DIR/"
echo cp LICENSE.txt "$INSTALL_DIR/"
echo.
echo # Make script executable
echo chmod +x "$INSTALL_DIR/dbassist.sh"
echo.
echo # Create desktop entry
echo echo "Creating desktop entry..."
echo cat ^> "$DESKTOP_FILE" ^<^<EOF
echo [Desktop Entry]
echo Version=1.0
echo Type=Application
echo Name=DBAssist
echo Comment=AI-Powered Database Assistant
echo Exec=$INSTALL_DIR/dbassist.sh
echo Icon=database
echo Terminal=false
echo Categories=Development;Database;
echo EOF
echo.
echo # Make desktop file executable
echo chmod +x "$DESKTOP_FILE"
echo.
echo # Update desktop database
echo if command -v update-desktop-database ^&^>/dev/null; then
echo     if [ "$EUID" -eq 0 ]; then
echo         update-desktop-database /usr/share/applications
echo     else
echo         update-desktop-database "$HOME/.local/share/applications"
echo     fi
echo fi
echo.
echo echo ""
echo echo "========================================"
echo echo "Installation Complete!"
echo echo "========================================"
echo echo ""
echo echo "DBAssist has been installed to: $INSTALL_DIR"
echo echo ""
echo echo "You can run it by:"
echo echo "1. Searching for 'DBAssist' in your application menu"
echo echo "2. Running: $INSTALL_DIR/dbassist.sh"
echo echo ""
) > "target\linux-package\install.sh"

REM Create README for Linux
(
echo # DBAssist for Linux
echo.
echo ## Quick Start
echo.
echo ### Option 1: Run Directly
echo ```bash
echo chmod +x dbassist.sh
echo ./dbassist.sh
echo ```
echo.
echo ### Option 2: Install System-Wide
echo ```bash
echo chmod +x install.sh
echo sudo ./install.sh
echo ```
echo Then search for "DBAssist" in your application menu.
echo.
echo ### Option 3: Install for Current User
echo ```bash
echo chmod +x install.sh
echo ./install.sh
echo ```
echo.
echo ## Requirements
echo - Java Runtime Environment ^(JRE^) 17 or higher
echo.
echo ### Install Java on Ubuntu/Debian:
echo ```bash
echo sudo apt update
echo sudo apt install openjdk-17-jre
echo ```
echo.
echo ### Install Java on Fedora/RHEL:
echo ```bash
echo sudo dnf install java-17-openjdk
echo ```
echo.
echo ### Or download from:
echo https://adoptium.net/
echo.
echo ## Uninstall
echo.
echo System-wide:
echo ```bash
echo sudo rm -rf /opt/dbassist
echo sudo rm /usr/share/applications/dbassist.desktop
echo ```
echo.
echo User installation:
echo ```bash
echo rm -rf ~/.local/share/dbassist
echo rm ~/.local/share/applications/dbassist.desktop
echo ```
) > "target\linux-package\README-LINUX.md"

echo Linux package prepared: target\linux-package\
echo.

REM ========================================
REM Step 5: Prepare Windows Installer
REM ========================================
echo ========================================
echo Step 5: Preparing Windows Installer...
echo ========================================

REM Copy files for installer input
if not exist "target\windows-installer\input" mkdir "target\windows-installer\input"
copy "target\DBAssist.jar" "target\windows-installer\input\DBAssist.jar"

REM Copy launcher script
copy "DBAssist-Launcher.bat" "target\windows-installer\input\DBAssist-Launcher.bat"

REM Create jpackage installer
echo Creating Windows installer with jpackage...
echo This may take several minutes...
echo.

jpackage ^
    --type exe ^
    --name "DBAssist" ^
    --app-version "1.0.0" ^
    --vendor "Masaddat Mallick" ^
    --description "AI-Powered Database Assistant for SQL Server and Oracle" ^
    --copyright "Copyright 2026 Masaddat Mallick" ^
    --input target\windows-installer\input ^
    --main-jar DBAssist.jar ^
    --main-class com.dbassist.dbassist.Launcher ^
    --dest target\windows-installer ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut ^
    --win-per-user-install ^
    --java-options "-Xmx1024m" ^
    --java-options "-Xms256m"

if %ERRORLEVEL% EQU 0 (
    echo Windows installer created: target\windows-installer\DBAssist-1.0.0.exe
) else (
    echo WARNING: jpackage failed. Installer not created.
    echo Make sure you have JDK 17+ with jpackage tool.
    echo You can still use the portable-jar or linux-package distributions.
)
echo.

REM ========================================
REM Step 6: Create ZIP archives
REM ========================================
echo ========================================
echo Step 6: Creating distribution archives...
echo ========================================

REM Create portable-jar ZIP
powershell -Command "Compress-Archive -Path 'target\portable-jar\*' -DestinationPath 'target\DBAssist-Portable-1.0.0.zip' -Force"
echo Created: target\DBAssist-Portable-1.0.0.zip

REM Create linux-package tar.gz
powershell -Command "Compress-Archive -Path 'target\linux-package\*' -DestinationPath 'target\DBAssist-Linux-1.0.0.zip' -Force"
echo Created: target\DBAssist-Linux-1.0.0.zip
echo Note: Convert to tar.gz on Linux with: tar czf DBAssist-Linux-1.0.0.tar.gz -C target/linux-package .
echo.

REM ========================================
REM Summary
REM ========================================
echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.
echo Distribution packages created:
echo.
echo 1. WINDOWS INSTALLER (target\windows-installer\)
echo    - DBAssist-1.0.0.exe
echo    - Double-click to install
echo    - Includes bundled JRE
echo    - ~150MB
echo.
echo 2. PORTABLE JAR (target\portable-jar\)
echo    - DBAssist.jar (double-click to run)
echo    - DBAssist.bat (Windows launcher)
echo    - DBAssist.command (Mac launcher)
echo    - dbassist.sh (Linux launcher)
echo    - Cross-platform
echo    - Requires JRE 17+
echo    - ~30MB
echo    - Archive: DBAssist-Portable-1.0.0.zip
echo.
echo 3. LINUX PACKAGE (target\linux-package\)
echo    - dbassist.sh (launcher with JRE detection)
echo    - install.sh (system installation)
echo    - Complete Linux integration
echo    - ~30MB
echo    - Archive: DBAssist-Linux-1.0.0.zip
echo.
echo ========================================
echo.
echo Quick Distribution:
echo - Windows users: DBAssist-1.0.0.exe
echo - All platforms: DBAssist-Portable-1.0.0.zip
echo - Linux users: DBAssist-Linux-1.0.0.zip
echo.
echo ========================================
pause

