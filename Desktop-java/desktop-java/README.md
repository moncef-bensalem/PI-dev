# NEXUS Desktop Application

This is the JavaFX desktop application for the NEXUS recruitment platform. It uses a local MySQL database to provide a rich desktop experience for HR managers, recruiters, and administrators.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/nexus/desktop/
│   │       ├── App.java              # Main JavaFX application class
│   │       ├── controller/
│   │       │   ├── PrimaryController.java # Main window controller
│   │       │   └── LoginController.java   # Login dialog controller
│   │       ├── dao/
│   │       │   ├── GenericDAO.java        # Generic DAO interface
│   │       │   └── UserDAO.java           # User database operations
│   │       ├── model/
│   │       │   └── User.java              # User data model
│   │       └── util/
│   │           └── DatabaseManager.java   # Database connection manager
│   ├── resources/
│   │   ├── fxml/
│   │   │   ├── primary.fxml             # Main application layout
│   │   │   └── login.fxml               # Login dialog layout
│   │   └── database.properties          # Database configuration
└── database/
    └── init.sql                         # Database initialization script
```

## Features

- **User Authentication**: Login functionality with local database
- **Database Integration**: Direct MySQL database connection
- **Modern UI**: JavaFX-based user interface
- **Responsive Design**: Adapts to different screen sizes
- **Connection Pooling**: Efficient database connection management

## Prerequisites

- Java 17 or higher
- Maven 3.6.0 or higher
- MySQL 8.0 or higher
- Local database setup

## Building and Running

### Using Maven

```bash
# Build the project
mvn clean package

# Run the application
mvn javafx:run
```

### Using IDE

Import the project as a Maven project in your favorite IDE (IntelliJ IDEA, Eclipse, etc.) and run the `com.nexus.desktop.App` class.

## Configuration

1. Create a MySQL database named `nexus_desktop`
2. Run the database initialization script: `database/init.sql`
3. Update database connection settings in `src/main/resources/database.properties`:
   ```
   db.host=localhost
   db.port=3306
   db.name=nexus_db
   db.username=your_username
   db.password=your_password
   ```

## Next Steps

- Add candidate management DAO and controllers
- Implement job offer and application management
- Add interview scheduling features
- Create comprehensive CRUD operations for all entities
- Add data validation and error handling
- Implement user role-based access control