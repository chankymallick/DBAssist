# DBAssist Build - Quick Reference

## 🚀 Build Commands

```cmd
# Build all 3 distributions (Recommended)
build-all.bat

# Quick JAR build only
build-jar.bat

# Full Windows installer only
build-installer.bat

# Development run
mvn javafx:run

# Maven package
mvn clean package
```

---

## 📦 Output Folders (target/)

| Folder | Contents | For |
|--------|----------|-----|
| `windows-installer/` | DBAssist-1.0.0.exe | Windows users (bundled JRE) |
| `portable-jar/` | DBAssist.jar + launchers | All platforms (requires JRE 17+) |
| `linux-package/` | Shell scripts + installer | Linux users |

### Distribution Archives

| File | Size | Platform |
|------|------|----------|
| `DBAssist-Portable-1.0.0.zip` | ~30MB | Windows/Mac/Linux |
| `DBAssist-Linux-1.0.0.zip` | ~30MB | Linux |
| `windows-installer/DBAssist-1.0.0.exe` | ~150MB | Windows |

---

## 🎯 Distribution Guide

### 1. Windows Installer (No Java Required)
**Location:** `target/windows-installer/DBAssist-1.0.0.exe`

**Features:**
- ✅ Bundled JRE (no Java installation needed)
- ✅ Professional Windows installer
- ✅ Start Menu + Desktop shortcuts
- ✅ Automatic uninstaller

**How Users Install:**
1. Download `DBAssist-1.0.0.exe`
2. Double-click to install
3. Follow installer wizard
4. Launch from Start Menu or Desktop

---

### 2. Portable JAR (Cross-Platform)
**Location:** `target/portable-jar/` or `DBAssist-Portable-1.0.0.zip`

**Features:**
- ✅ Works on Windows, Mac, and Linux
- ✅ No installation required
- ✅ Double-click to run (if JRE installed)
- ✅ Multiple launcher scripts included

**How Users Run:**

**Windows:**
- Double-click `DBAssist.jar`
- Or run `DBAssist.bat`

**Mac:**
- Double-click `DBAssist.command`
- Or: `chmod +x DBAssist.command && ./DBAssist.command`

**Linux:**
- Run: `chmod +x dbassist.sh && ./dbassist.sh`
- Or: `java -jar DBAssist.jar`

---

### 3. Linux Package (Native Linux)
**Location:** `target/linux-package/` or `DBAssist-Linux-1.0.0.zip`

**Features:**
- ✅ Shell script with JRE detection
- ✅ System-wide or user installation
- ✅ Desktop menu integration
- ✅ Proper Linux packaging

**How Users Install:**

**Quick Run:**
```bash
chmod +x dbassist.sh
./dbassist.sh
```

**System Installation:**
```bash
chmod +x install.sh
sudo ./install.sh
```

**User Installation:**
```bash
chmod +x install.sh
./install.sh
```

---

## ✅ Testing

```cmd
# Test Windows Installer
target\windows-installer\DBAssist-1.0.0.exe

# Test Portable JAR (Windows)
cd target\portable-jar
DBAssist.bat

# Test Portable JAR (command line)
cd target\portable-jar
java -jar DBAssist.jar

# Test Linux (on Linux/WSL)
cd target/linux-package
chmod +x dbassist.sh
./dbassist.sh
```

---

## 🔍 JRE Detection

### Windows Launcher
- ✅ Checks JAVA_HOME
- ✅ Checks PATH
- ✅ Validates version (17+)
- ✅ Shows installation guide

### Linux Launcher  
- ✅ Checks JAVA_HOME
- ✅ Checks command -v java
- ✅ Validates version (17+)
- ✅ Shows apt/dnf install commands

---

## 📋 Requirements

### For Building
- JDK 17+
- Maven 3.6+
- Windows 10/11 (for full build)

### For End Users

**Windows Installer:**
- Windows 10/11
- **No Java needed** ✅

**Portable JAR:**
- JRE 17+
- Windows/Mac/Linux

**Linux Package:**
- JRE 17+
- Ubuntu/Debian/Fedora/RHEL

---

## 👤 Developer

**Name:** Masaddat Mallick  
**Email:** masaddat.mallick@gmail.com  
**License:** Open Source (MIT)

---

## 📚 Documentation

- `BUILD-GUIDE.md` - Complete build guide
- `INSTALLER-README.md` - Installer documentation  
- `PRODUCTION-BUILD-SUMMARY.md` - Complete summary
- `LICENSE.txt` - MIT License

---

## 🆘 Quick Fixes

**Java not found:**
```cmd
# Windows
setx JAVA_HOME "C:\Program Files\Java\jdk-17"

# Linux
sudo apt install openjdk-17-jre
# or
sudo dnf install java-17-openjdk
```

**Build failed:**
```cmd
mvn clean install -U
```

**jpackage not found:**
- Use JDK 17+ (not JRE)
- jpackage included in JDK 17+

---

## 📁 Complete Folder Structure

```
target/
├── windows-installer/
│   ├── input/
│   │   ├── DBAssist.jar
│   │   └── DBAssist-Launcher.bat
│   └── DBAssist-1.0.0.exe          # Windows installer
│
├── portable-jar/
│   ├── DBAssist.jar                 # Main JAR
│   ├── DBAssist.bat                 # Windows launcher
│   ├── DBAssist.command             # Mac launcher
│   ├── INSTALL.txt                  # Instructions
│   ├── README.md
│   └── LICENSE.txt
│
├── linux-package/
│   ├── DBAssist.jar                 # Main JAR
│   ├── dbassist.sh                  # Linux launcher
│   ├── install.sh                   # Installation script
│   ├── README-LINUX.md              # Linux instructions
│   ├── README.md
│   └── LICENSE.txt
│
├── DBAssist-Portable-1.0.0.zip     # Portable distribution
├── DBAssist-Linux-1.0.0.zip        # Linux distribution
└── DBAssist.jar                     # Original JAR
```

---

*DBAssist v1.0.0 - February 2026*



