# 🗄️ DBAssistant

**AI-Powered Database Explorer for SQL Server and Oracle**

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE.txt)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17-green.svg)](https://openjfx.io/)
[![Version](https://img.shields.io/badge/version-1.0.0-brightgreen.svg)](https://github.com/masaddat/DBAssistant/releases)

> A modern, intuitive database management tool with advanced features including SQL worksheets, data comparison, intelligent auto-completion, and more.

🌐 **[Visit Official Website](https://masaddat.github.io/DBAssistant/)**

---

## ✨ Features

### 🎯 Core Features
- **SQL Worksheet** - Advanced SQL editor with syntax highlighting and auto-completion
- **Smart Data Comparison** - Compare tables or query results with visual diff highlighting
- **Connection Manager** - Save, clone, and manage multiple database connections
- **Modern Data Grid** - Professional grid with filtering, sorting, and column selection
- **Auto-Save Everything** - Queries, filters, and configurations saved automatically
- **Export & Reports** - Export to Excel, HTML, and other formats

### 🚀 Advanced Features
- **Intelligent Auto-Completion** - Table and column suggestions as you type
- **Query Result Grid** - Execute queries and view results in filterable grids
- **Data Comparison** - Row-by-row comparison with configurable matching columns
- **Connection Switching** - Switch connections while preserving tab state
- **Tab Cloning** - Duplicate tabs with all filters and settings
- **Progress Indicators** - Visual feedback for all intensive operations

### 💎 Quality of Life
- **Professional UI** - Clean, modern interface with custom title bar
- **Persistent Storage** - All work saved automatically to disk
- **Keyboard Shortcuts** - Efficient navigation with hotkeys
- **Responsive Design** - Works great on any screen size
- **Dark Theme Ready** - Eye-friendly color schemes

---

## 📸 Screenshots

| Home Screen | SQL Worksheet | Data Grid |
|------------|---------------|-----------|
| ![Home](UIScreenShots/Screenshot%202026-02-15%20100701.png) | ![Worksheet](UIScreenShots/Screenshot%202026-02-15%20100806.png) | ![Grid](UIScreenShots/Screenshot%202026-02-15%20100857.png) |

| Data Comparison | Connection Manager |
|----------------|-------------------|
| ![Comparison](UIScreenShots/Screenshot%202026-02-15%20102222.png) | ![Connections](UIScreenShots/Screenshot%202026-02-15%20102304.png) |

---

## 🚀 Quick Start

### Prerequisites
- **Java 17 or higher** (JRE or JDK)
- **Database Access** - SQL Server 2012+ or Oracle 11g+
- **Operating System** - Windows 10+, Linux, or macOS

### Installation

#### Option 1: Windows Installer (Recommended)
1. Download `DBAssistant-Setup.exe` from [Releases](https://github.com/masaddat/DBAssistant/releases)
2. Run the installer
3. Follow the installation wizard
4. Launch from Start Menu

#### Option 2: Portable JAR
1. Download `DBAssistant-Portable.zip` from [Releases](https://github.com/masaddat/DBAssistant/releases)
2. Extract the ZIP file
3. Double-click `DBAssist.jar` or run:
   ```bash
   java -jar DBAssist.jar
   ```

#### Option 3: Linux
1. Download `DBAssistant-Linux.zip` from [Releases](https://github.com/masaddat/DBAssistant/releases)
2. Extract and run:
   ```bash
   chmod +x install.sh
   ./install.sh
   ```

---

## 📖 Usage Guide

### Adding a Connection
1. Click **"+ New Connection"** on home screen
2. Select database type (SQL Server or Oracle)
3. Enter connection details:
   - Host, Port, Database Name
   - Username and Password
4. Click **"Test Connection"**
5. Click **"Save"** if successful

### Using SQL Worksheet
1. Right-click on connection → **"📝 New SQL Worksheet"**
2. Type your SQL query (with auto-completion!)
3. Press **Ctrl+Enter** to execute
4. Click **"📊 Open in Grid"** to view results in a filterable grid

### Comparing Data
1. Open 2+ table data tabs
2. Click **"⚖ Compare Tables"** button
3. Select source and target tabs
4. Choose columns for row identification
5. View comparison with visual highlights

### Managing Worksheets
- All queries **auto-saved** as you type
- Saved to: `~/.dbassist/worksheets/`
- Restore automatically when reopened
- Never lose your work!

---

## 🛠️ Building from Source

### Requirements
- Java 17 or higher
- Maven 3.6+
- JavaFX 17

### Build Commands

```bash
# Clean and compile
mvn clean compile

# Build JAR
mvn clean package

# Build all distributions
./build-all.bat  # Windows
./build-all.sh   # Linux/Mac

# Run application
mvn javafx:run
```

### Build Outputs

After running `build-all`:
- **Windows Installer**: `target/windows-installer/DBAssistant-Setup.exe`
- **Portable JAR**: `target/portable-jar/DBAssistant-Portable.zip`
- **Linux Package**: `target/linux-package/DBAssistant-Linux.zip`

---

## 📂 Project Structure

```
DBAssistant/
├── src/main/java/
│   └── com/dbassist/dbassist/
│       ├── components/           # UI components
│       │   ├── SqlWorksheet.java
│       │   ├── TableDataGrid.java
│       │   ├── QueryResultGrid.java
│       │   └── ComparisonResultView.java
│       ├── service/              # Business logic
│       │   ├── ConnectionManager.java
│       │   ├── WorksheetManager.java
│       │   ├── DataComparisonService.java
│       │   └── DatabaseMetadataService.java
│       ├── model/                # Data models
│       │   ├── DatabaseConnection.java
│       │   ├── DataTabConfig.java
│       │   └── ComparisonResult.java
│       └── HomeController.java   # Main controller
├── src/main/resources/
│   └── com/dbassist/dbassist/
│       ├── home-view.fxml
│       ├── styles.css
│       └── sql-worksheet.css
├── UIScreenShots/                # App screenshots
├── index.html                    # GitHub Pages site
├── pom.xml                       # Maven config
└── README.md                     # This file
```

---

## 🗄️ Data Storage

DBAssistant stores data in your home directory:

```
~/.dbassist/
├── connections.dat       # Saved connections (encrypted passwords)
├── tabs/                 # Tab configurations
│   └── *.json           # Filter and column settings
└── worksheets/           # SQL worksheet content
    └── *.sql            # Auto-saved queries
```

### Backup Your Data
```bash
# Windows
xcopy /E /I %USERPROFILE%\.dbassist %USERPROFILE%\.dbassist.backup

# Linux/Mac
cp -r ~/.dbassist ~/.dbassist.backup
```

---

## ⚙️ Configuration

### Database Drivers
Included drivers:
- **SQL Server**: Microsoft JDBC Driver 12.4.2
- **Oracle**: Oracle JDBC Driver 23.3.0

### JVM Options
For better performance, you can add JVM options:
```bash
java -Xmx2g -Xms512m -jar DBAssist.jar
```

---

## 🔧 Troubleshooting

### Connection Issues
- Verify database server is running
- Check firewall settings
- Ensure correct port number
- Test with other SQL clients first

### Java Not Found
```bash
# Check Java version
java -version

# Should show Java 17 or higher
# If not, download from: https://adoptium.net/
```

### Performance Issues
- Limit max rows in table view (default: 1000)
- Close unused tabs
- Increase JVM memory (see JVM Options)

### Worksheet Not Saving
- Check disk space
- Verify write permissions on `~/.dbassist/worksheets/`
- Check console for error messages

---

## 🤝 Contributing

Contributions are welcome! Here's how:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Setup
```bash
# Clone repository
git clone https://github.com/masaddat/DBAssistant.git
cd DBAssistant

# Build and run
mvn clean compile
mvn javafx:run
```

---

## 📝 Documentation

- **[User Guide](SQL_WORKSHEET_USER_GUIDE.md)** - Complete usage instructions
- **[Build Guide](BUILD-GUIDE.md)** - Building from source
- **[Feature Docs](WORKSHEET_PERSISTENCE.md)** - Detailed feature documentation
- **[GitHub Pages Setup](GITHUB_PAGES_SETUP.md)** - Deploying the website

---

## 🐛 Bug Reports & Feature Requests

Found a bug or have a feature request?

1. Check [existing issues](https://github.com/masaddat/DBAssistant/issues)
2. If not found, [create a new issue](https://github.com/masaddat/DBAssistant/issues/new)
3. Provide:
   - Description
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots if applicable
   - Your OS and Java version

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

### Third-Party Libraries
- JavaFX 17 - [GPL with Classpath Exception](https://openjfx.io/)
- RichTextFX - [BSD License](https://github.com/FXMisc/RichTextFX)
- Apache POI - [Apache License 2.0](https://poi.apache.org/)
- Ikonli - [Apache License 2.0](https://kordamp.org/ikonli/)

---

## 👨‍💻 Author

**Masaddat Mallick**
- Email: [masaddat.mallick@gmail.com](mailto:masaddat.mallick@gmail.com)
- GitHub: [@masaddat](https://github.com/masaddat)
- LinkedIn: [linkedin.com/in/masaddat](https://linkedin.com/in/masaddat)

---

## 🌟 Acknowledgments

- Thanks to all contributors
- JavaFX community for excellent documentation
- Open source projects that made this possible

---

## 📊 Project Stats

- **Lines of Code**: ~15,000
- **Components**: 20+
- **Features**: 30+
- **Documentation Pages**: 15+
- **Build Time**: ~5 seconds
- **Package Size**: ~50 MB

---

## 🎯 Roadmap

### Planned Features
- [ ] More database support (MySQL, PostgreSQL)
- [ ] Cloud sync for worksheets
- [ ] Query history and favorites
- [ ] Visual query builder
- [ ] Schema designer
- [ ] Dark/Light theme toggle
- [ ] Plugin system
- [ ] Multi-language support

### In Progress
- [x] SQL Worksheet persistence
- [x] Data comparison feature
- [x] Auto-completion
- [x] Export functionality

---

## 💬 Community

Join the discussion:
- [GitHub Discussions](https://github.com/masaddat/DBAssistant/discussions)
- [Issue Tracker](https://github.com/masaddat/DBAssistant/issues)

---

## ⭐ Star History

If you find this project useful, please consider giving it a star! ⭐

---

<div align="center">

**Made with ❤️ by Masaddat Mallick**

[Website](https://masaddat.github.io/DBAssistant/) • 
[Download](https://github.com/masaddat/DBAssistant/releases) • 
[Documentation](SQL_WORKSHEET_USER_GUIDE.md) • 
[Report Bug](https://github.com/masaddat/DBAssistant/issues)

</div>

