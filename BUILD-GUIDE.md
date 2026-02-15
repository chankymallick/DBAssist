# DBAssist Production Build Guide

## Building the Windows Installer

### Prerequisites

1. **JDK 17 or Higher**
   - Download from: https://adoptium.net/
   - Ensure `JAVA_HOME` is set
   - Ensure `java` and `javac` are in PATH

2. **Maven 3.6+**
   - Download from: https://maven.apache.org/download.cgi
   - Ensure `mvn` is in PATH

3. **Inno Setup (Optional - for advanced installer)**
   - Download from: https://jrsoftware.org/isdl.php
   - Only needed if using the .iss script

---

## Build Methods

### Method 1: Using build-installer.bat (Recommended)

This method uses jpackage (included with JDK 17+) to create a native Windows installer.

```cmd
# Run the build script
build-installer.bat
```

**What it does:**
1. Builds the Maven project
2. Creates a fat JAR with all dependencies
3. Uses jpackage to create a Windows .exe installer
4. The installer includes a bundled JRE

**Output:**
- `installer/output/DBAssist-1.0.0.exe` - Windows installer

**Advantages:**
- ✅ Bundles JRE (no need for users to install Java)
- ✅ Creates Start Menu shortcuts
- ✅ Creates Desktop shortcut
- ✅ Professional Windows installer
- ✅ Automatic uninstaller

---

### Method 2: Using Inno Setup (Advanced)

This method creates a more customizable installer with JRE checking.

```cmd
# Step 1: Build the JAR
mvn clean package

# Step 2: Compile the Inno Setup script
# Open dbassist-installer.iss in Inno Setup
# Click "Build" -> "Compile"
```

**Output:**
- `installer/output/DBAssist-Setup-1.0.0.exe` - Custom installer

**Advantages:**
- ✅ Checks for Java before installation
- ✅ Redirects to Java download if not found
- ✅ More control over installation process
- ✅ Custom dialogs and messages
- ✅ Smaller installer size (no bundled JRE)

---

### Method 3: Manual JAR Distribution

If you just want to distribute the JAR file:

```cmd
# Build the fat JAR
mvn clean package

# The JAR will be at:
# target/DBAssist.jar
```

**Distribution:**
1. Copy `target/DBAssist.jar` to distribution folder
2. Copy `DBAssist-Launcher.bat` to distribution folder
3. Create a ZIP file with both files
4. Users can extract and run `DBAssist-Launcher.bat`

**The launcher will:**
- Check for Java installation
- Check Java version (requires JRE 17+)
- Show detailed error messages if Java is missing
- Guide users to install Java

---

## Installation Requirements for End Users

### If Using jpackage Installer (Method 1)
- **No JRE required** - Bundled with installer
- **Windows 10/11**
- **~150MB disk space**

### If Using Inno Setup Installer (Method 2)
- **JRE 17 or higher required**
- **Windows 10/11**
- **~50MB disk space**

### If Using Manual JAR (Method 3)
- **JRE 17 or higher required**
- **Windows/Mac/Linux**
- **~30MB disk space**

---

## Build Outputs

After successful build, you'll find:

```
installer/
├── input/
│   └── DBAssist.jar          # Fat JAR with dependencies
└── output/
    └── DBAssist-1.0.0.exe    # Windows installer
```

---

## Testing the Build

### Test the Launcher
```cmd
# Copy files to test location
mkdir test-install
copy target\DBAssist.jar test-install\
copy DBAssist-Launcher.bat test-install\

# Run the launcher
cd test-install
DBAssist-Launcher.bat
```

### Test the Installer
```cmd
# Run the installer
installer\output\DBAssist-1.0.0.exe

# Follow installation wizard
# Launch from Start Menu or Desktop shortcut
```

---

## Distribution

### For End Users (Non-Technical)
**Recommended: Use Method 1 (jpackage)**
- Single .exe file
- Double-click to install
- No Java installation needed

### For Technical Users
**Recommended: Use Method 3 (JAR + Launcher)**
- Smaller download size
- More flexible
- Users can use their existing Java installation

### For Corporate Deployment
**Recommended: Use Method 2 (Inno Setup)**
- Can be customized for corporate requirements
- Silent installation support
- Group Policy deployment ready

---

## Troubleshooting

### "JAVA_HOME is not set"
```cmd
# Set JAVA_HOME
setx JAVA_HOME "C:\Program Files\Java\jdk-17"
```

### "Maven build failed"
```cmd
# Check Maven installation
mvn -version

# Clean Maven cache
mvn clean install -U
```

### "jpackage not found"
- Ensure you're using JDK 17+ (not JRE)
- jpackage is included in JDK 17+
- Check: `jpackage --version`

### Installer doesn't detect Java
- The launcher script checks for Java automatically
- It looks for JAVA_HOME and PATH
- Shows detailed instructions if Java is missing

---

## Customization

### Change Application Version
Edit `pom.xml`:
```xml
<version>1.0.0</version>
```

### Change Application Icon
Replace `src/main/resources/icon.ico` with your icon

### Change Installer Details
Edit `build-installer.bat`:
- `--app-version`
- `--vendor`
- `--description`

---

## Support

- **Developer:** Masaddat Mallick
- **Email:** masaddat.mallick@gmail.com
- **License:** Open Source (MIT)

---

## Build Checklist

Before building the installer:

- [ ] Update version in `pom.xml`
- [ ] Test application with `mvn javafx:run`
- [ ] Run tests: `mvn test`
- [ ] Update README.md if needed
- [ ] Update CHANGELOG.md
- [ ] Create application icon
- [ ] Test on clean Windows machine
- [ ] Verify all features work
- [ ] Check file size of installer
- [ ] Test installation process
- [ ] Test uninstallation process
- [ ] Verify shortcuts work
- [ ] Test on different Windows versions

---

## Quick Start for Building

```cmd
# One command to build everything
build-installer.bat

# Output will be at:
# installer\output\DBAssist-1.0.0.exe

# Test it:
installer\output\DBAssist-1.0.0.exe
```

**That's it!** Your Windows installer is ready for distribution.

