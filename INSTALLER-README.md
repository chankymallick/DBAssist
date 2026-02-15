# DBAssist - Windows Installer Package

## 🎯 Production Build Complete!

Your DBAssist application is now ready for production deployment with a professional Windows installer.

---

## 📦 What's Included

### Build Scripts

1. **build-installer.bat** - Creates full Windows installer with bundled JRE
2. **build-jar.bat** - Quick build for JAR distribution  
3. **DBAssist-Launcher.bat** - Smart launcher with JRE checking

### Installer Scripts

4. **dbassist-installer.iss** - Inno Setup script for custom installer

### Documentation

5. **BUILD-GUIDE.md** - Complete build instructions
6. **LICENSE.txt** - MIT License
7. **README.md** - Application documentation

---

## 🚀 Quick Start - Build the Installer

### Option 1: Full Windows Installer (Recommended)

```cmd
build-installer.bat
```

**Output:** `installer/output/DBAssist-1.0.0.exe`

**Features:**
- ✅ Bundled JRE (no Java installation needed)
- ✅ Professional Windows installer
- ✅ Start Menu shortcuts
- ✅ Desktop shortcut
- ✅ Automatic uninstaller
- ✅ ~150MB size

---

### Option 2: JAR with Smart Launcher

```cmd
build-jar.bat
```

**Output:** `target/DBAssist.jar` + `DBAssist-Launcher.bat`

**Features:**
- ✅ Checks for Java before running
- ✅ Shows detailed error if Java missing
- ✅ Guides users to install Java
- ✅ Requires JRE 17+
- ✅ ~30MB size

**Distribute:**
1. Copy `target/DBAssist.jar`
2. Copy `DBAssist-Launcher.bat`
3. ZIP both files
4. Users extract and run `DBAssist-Launcher.bat`

---

## 🔍 JRE Detection Features

### The Launcher Automatically:

1. **Checks if Java is installed**
   - Looks for JAVA_HOME environment variable
   - Checks if `java` is in system PATH

2. **Validates Java version**
   - Requires JRE 17 or higher
   - Shows current version if too old

3. **Provides detailed error messages**
   ```
   ERROR: Java Not Found
   
   DBAssist requires Java Runtime Environment (JRE) 17 or higher.
   
   Please follow these steps:
   1. Download JRE 17 or higher from:
      https://adoptium.net/
   2. Install Java Runtime Environment
   3. Set JAVA_HOME environment variable
   4. Add Java to PATH
   5. Restart this application
   ```

4. **Guides users through installation**
   - Step-by-step instructions
   - Download links
   - Environment variable setup guide

---

## 📋 System Requirements

### For End Users

**Using Windows Installer (build-installer.bat):**
- Windows 10/11
- 150MB disk space
- No Java installation required ✅

**Using JAR + Launcher (build-jar.bat):**
- Windows 10/11
- JRE 17 or higher
- 30MB disk space

---

## 🛠️ For Developers

### Prerequisites

1. **JDK 17+** 
   - https://adoptium.net/
   - Set JAVA_HOME

2. **Maven 3.6+**
   - https://maven.apache.org/

3. **Inno Setup** (Optional)
   - https://jrsoftware.org/isdl.php
   - Only for custom installer

### Build Commands

```cmd
# Development run
mvn javafx:run

# Quick JAR build
build-jar.bat

# Full installer
build-installer.bat

# Maven package only
mvn clean package
```

---

## 📁 Project Structure

```
DBAssist/
├── src/                          # Source code
├── target/                       # Build output
│   └── DBAssist.jar             # Executable JAR
├── installer/                    # Installer output
│   ├── input/                   # Installer input files
│   └── output/                  # Generated installers
│       └── DBAssist-1.0.0.exe   # Windows installer
├── build-installer.bat          # Build full installer
├── build-jar.bat                # Build JAR only
├── DBAssist-Launcher.bat        # Smart launcher script
├── dbassist-installer.iss       # Inno Setup script
├── LICENSE.txt                   # MIT License
├── BUILD-GUIDE.md               # Build documentation
└── pom.xml                      # Maven configuration
```

---

## ✅ What the Installer Does

### Installation Process

1. **Checks for Java** (if using Inno Setup installer)
   - Validates JRE is installed
   - Shows download link if not found

2. **User selects installation directory**
   - Default: `C:\Program Files\DBAssist`
   - User can change

3. **Copies application files**
   - DBAssist.jar
   - Launcher script
   - Documentation

4. **Creates shortcuts**
   - Start Menu: DBAssist
   - Desktop: DBAssist (optional)
   - Start Menu: Uninstall DBAssist

5. **Registers uninstaller**
   - Appears in Windows "Apps & Features"
   - Clean uninstallation support

---

## 🎨 Customization

### Change Version

Edit `pom.xml`:
```xml
<version>1.0.0</version>
```

### Change Installer Name

Edit `build-installer.bat`:
```bat
--name "DBAssist" ^
--app-version "1.0.0" ^
--vendor "Masaddat Mallick" ^
```

### Change Application Icon

Replace: `src/main/resources/icon.ico`

---

## 🧪 Testing

### Test the Launcher

```cmd
mkdir test
copy target\DBAssist.jar test\
copy DBAssist-Launcher.bat test\
cd test
DBAssist-Launcher.bat
```

### Test Without Java

1. Temporarily rename Java folder
2. Run `DBAssist-Launcher.bat`
3. Should show error message with instructions
4. Restore Java folder

### Test the Installer

```cmd
installer\output\DBAssist-1.0.0.exe
```

1. Follow installation wizard
2. Check Start Menu shortcut
3. Check Desktop shortcut (if selected)
4. Launch application
5. Check "Apps & Features" for uninstaller

---

## 📤 Distribution

### For Non-Technical Users

**Use:** `build-installer.bat`

**Why:**
- One-click installation
- No Java knowledge required
- Professional experience

### For Technical Users

**Use:** `build-jar.bat`

**Why:**
- Smaller download
- Can use existing Java
- More flexible

### For Corporate

**Use:** Inno Setup script

**Why:**
- Silent installation support
- Group Policy compatible
- Customizable

---

## 🆘 Troubleshooting

### Build Errors

**"JAVA_HOME is not set"**
```cmd
setx JAVA_HOME "C:\Program Files\Java\jdk-17"
```

**"Maven not found"**
- Install Maven
- Add to PATH

**"jpackage not found"**
- Use JDK 17+ (not JRE)
- jpackage included in JDK 17+

### Runtime Errors

**"Java not found"**
- Launcher shows detailed instructions
- Install JRE 17+ from https://adoptium.net/

**"Java version too old"**
- Launcher shows current version
- Upgrade to JRE 17+

---

## 📞 Support

**Developer:** Masaddat Mallick  
**Email:** masaddat.mallick@gmail.com  
**License:** Open Source (MIT)

---

## 🎉 Success!

Your production build is ready! Choose your distribution method:

1. **Full Installer** - Best for end users
2. **JAR + Launcher** - Best for technical users  
3. **Custom Installer** - Best for corporate

All methods include JRE checking and user-friendly error messages!

---

*Last Updated: February 15, 2026*
*DBAssist Version: 1.0.0*

