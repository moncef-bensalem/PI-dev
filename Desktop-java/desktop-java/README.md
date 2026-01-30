# NEXUS Desktop Application

This is the JavaFX desktop application for the NEXUS recruitment platform. It connects to the Symfony backend API to provide a rich desktop experience for HR managers, recruiters, and administrators.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/nexus/desktop/
│   │       ├── App.java              # Main JavaFX application class
│   │       ├── PrimaryController.java # Main window controller
│   │       └── LoginController.java   # Login dialog controller
│   ├── resources/
│   │   ├── primary.fxml             # Main application layout
│   │   └── login.fxml               # Login dialog layout
└── model/
│   └── User.java                    # User data model
└── service/
    └── ApiService.java              # API communication service
```

## Features

- **User Authentication**: Login functionality to access the platform
- **API Integration**: Communication with Symfony backend
- **Modern UI**: JavaFX-based user interface
- **Responsive Design**: Adapts to different screen sizes

## Prerequisites

- Java 17 or higher
- Maven 3.6.0 or higher
- Access to the Symfony backend API (running on http://localhost:8000)

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

The application expects the Symfony backend to be running at `http://localhost:8000`. If your backend runs on a different port or host, update the `BASE_URL` constant in `ApiService.java`.

## Next Steps

- Implement full authentication with JWT tokens
- Add candidate management screens
- Implement job offer and application management
- Add interview scheduling features
- Integrate with calendar applications