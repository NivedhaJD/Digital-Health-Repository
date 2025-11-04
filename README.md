# Digital Health Repository

A comprehensive healthcare management system built with Java 11 backend and modern web frontend.

## 📁 Project Structure

```
Digital-Health-Repository/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── digitalhealth/
│   │   │           ├── api/              # REST API server
│   │   │           ├── cli/              # Command-line interface
│   │   │           ├── dao/              # Data access layer
│   │   │           │   └── file/         # File-based persistence
│   │   │           ├── dto/              # Data transfer objects
│   │   │           ├── exception/        # Custom exceptions
│   │   │           ├── facade/           # Backend facade pattern
│   │   │           ├── model/            # Domain models
│   │   │           ├── service/          # Business logic
│   │   │           └── util/             # Utilities
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/                         # Unit tests
├── frontend/
│   ├── html/                             # HTML pages
│   ├── css/                              # Stylesheets
│   └── js/                               # JavaScript files
├── docs/                                 # Documentation
├── export/                               # Data exports
├── data/                                 # File-based data storage
├── pom.xml                               # Maven configuration
└── README.md

```

## 🚀 Features

- **Patient Management**: Register and manage patient records
- **Doctor Management**: Manage doctor profiles and specialties
- **Appointment Scheduling**: Book, view, and manage appointments
- **Health Records**: Maintain comprehensive medical histories
- **File-based Persistence**: Reliable data storage using flat files
- **REST API**: RESTful API for frontend integration
- **CLI Interface**: Command-line interface for direct backend access

## 🛠️ Technologies

- **Backend**: Java 11, Maven
- **Frontend**: HTML5, CSS3, JavaScript
- **Persistence**: File-based storage (.dat files)
- **Testing**: JUnit 4
- **API**: Custom HTTP server (port 8080)

## 📋 Prerequisites

- Java 11 or higher
- Maven 3.6+
- Modern web browser

## 🔧 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/mathewgeejo/Digital-Health-Repository.git
   cd Digital-Health-Repository
   ```

2. **Build the project**
   ```bash
   mvn clean package
   ```

3. **Run the application**
   
   **Option A: API Server (for web frontend)**
   ```bash
   java -cp target/digital-health-backend-1.0.0.jar com.digitalhealth.api.ApiServer
   ```
   
   **Option B: CLI Application**
   ```bash
   java -jar target/digital-health-backend-1.0.0.jar
   ```

4. **Access the web interface**
   - Open `frontend/html/index.html` in your browser
   - Or access via `http://localhost:8080` when API server is running

## 📖 Usage

### Web Interface

1. Start the API server
2. Open `frontend/html/index.html` in your browser
3. Navigate between Patient, Doctor, and Admin interfaces
4. Use the forms to manage patients, doctors, and appointments

### CLI Interface

Run the JAR file and follow the interactive menu:
- Manage patients
- Manage doctors
- Book and manage appointments
- View health records
- Export patient histories

## 🏗️ Architecture

The system follows a layered architecture:

1. **Presentation Layer**: HTML/CSS/JavaScript frontend
2. **API Layer**: REST API server
3. **Service Layer**: Business logic
4. **DAO Layer**: Data access abstraction
5. **Persistence Layer**: File-based storage

## 🧪 Testing

Run the test suite:
```bash
mvn test
```

## 📚 Documentation

Detailed documentation is available in the `docs/` directory:
- Backend architecture and API details
- Integration guides
- System documentation
- Quick start guides

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is part of an academic assignment (APP Project Sem 3).

## 👥 Authors

- Mathew Geejo (@mathewgeejo)

## 📞 Support

For issues and questions, please open an issue on GitHub.
