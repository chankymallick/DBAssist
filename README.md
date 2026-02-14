# DBAssist - Your AI-Powered Database Assistant

DBAssist is a generic database explorer with AI integration designed to work as an intelligent agent to directly operate various databases.

## Features

### Home Page ✨
The home page provides a clean, modern interface with the following sections:

#### 1. **New Connection** 🔌
- Connect to new databases
- Support for multiple database types:
  - MySQL
  - PostgreSQL
  - MongoDB
  - Oracle
  - SQL Server

#### 2. **Recent Connections** 📚
- Quick access to previously saved database connections
- Connection history management

#### 3. **AI Assistant** 🤖
- AI-powered query generation (Coming Soon)
- Natural language to SQL conversion
- Intelligent database operations

#### 4. **Documentation** 📖
- User guides and tutorials
- API documentation
- Best practices

## Project Structure

```
DBAssist/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/dbassist/dbassist/
│   │   │       ├── HelloApplication.java    # Main application entry point
│   │   │       ├── HomeController.java      # Home page controller
│   │   │       ├── HelloController.java     # Legacy controller
│   │   │       └── Launcher.java            # Application launcher
│   │   └── resources/
│   │       └── com/dbassist/dbassist/
│   │           ├── home-view.fxml           # Home page UI layout
│   │           ├── styles.css               # Application styles
│   │           └── hello-view.fxml          # Legacy view
│   └── test/
├── pom.xml                                  # Maven configuration
└── README.md                                # This file
```

## Technology Stack

- **Java 17**: Core programming language
- **JavaFX 17**: UI framework
- **Maven**: Build and dependency management
- **ControlsFX**: Additional JavaFX controls
- **FormsFX**: Form handling
- **Ikonli**: Icon library

## Running the Application

### Using Maven
```bash
mvn clean javafx:run
```

### Using IDE
Run the `Launcher.java` class as the main entry point.

## Development Roadmap

### Phase 1: Foundation ✅
- [x] Home page UI
- [x] Basic project structure
- [ ] Connection management system
- [ ] Database connection pools

### Phase 2: Database Integration
- [ ] MySQL connector
- [ ] PostgreSQL connector
- [ ] MongoDB connector
- [ ] Oracle connector
- [ ] SQL Server connector

### Phase 3: Core Features
- [ ] Database schema explorer
- [ ] Query editor with syntax highlighting
- [ ] Result set viewer
- [ ] Export data functionality

### Phase 4: AI Integration
- [ ] AI agent framework
- [ ] Natural language query processing
- [ ] Query optimization suggestions
- [ ] Automated database operations

### Phase 5: Advanced Features
- [ ] Multi-database operations
- [ ] Data migration tools
- [ ] Performance monitoring
- [ ] Backup and restore

## Contributing

This is a personal project under active development. More information about contributing will be added soon.

## License

Copyright © 2026 DBAssist. All rights reserved.

## Version History

- **v1.0.0-SNAPSHOT** (Current)
  - Initial home page implementation
  - Modern UI design
  - Basic navigation structure

