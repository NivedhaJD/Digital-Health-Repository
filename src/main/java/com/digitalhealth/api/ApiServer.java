package com.digitalhealth.api;

import com.digitalhealth.dto.*;
import com.digitalhealth.exception.*;
import com.digitalhealth.facade.BackendFacade;
import com.digitalhealth.facade.BackendFactory;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple HTTP REST API Server for Digital Health Repository
 * Connects the Java backend with HTML/JavaScript frontend
 */
public class ApiServer {
    private final BackendFacade facade;
    private final HttpServer server;
    private static final int PORT = 8080;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ApiServer() throws IOException {
        this.facade = BackendFactory.create();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        setupRoutes();
    }

    private void setupRoutes() {
        // CORS and static files
        server.createContext("/", this::handleStaticFiles);
        
        // Authentication endpoints
        server.createContext("/api/auth/register", this::handleAuthRegister);
        server.createContext("/api/auth/login", this::handleAuthLogin);
        server.createContext("/api/auth/validate", this::handleAuthValidate);
        
        // Patient endpoints
        server.createContext("/api/patients", this::handlePatients);
        server.createContext("/api/patients/register", this::handleRegisterPatient);
        
        // Doctor endpoints
        server.createContext("/api/doctors", this::handleDoctors);
        server.createContext("/api/doctors/register", this::handleRegisterDoctor);
        
        // Appointment endpoints
        server.createContext("/api/appointments", this::handleAppointments);
        server.createContext("/api/appointments/book", this::handleBookAppointment);
        server.createContext("/api/appointments/cancel", this::handleCancelAppointment);
        
        // Health records endpoints
        server.createContext("/api/health-records", this::handleHealthRecords);
        server.createContext("/api/health-records/add", this::handleAddHealthRecord);
    }

    private void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) path = "/html/index.html";
        
        // Map paths to frontend directory structure
        String filePath;
        if (path.startsWith("/html/") || path.endsWith(".html")) {
            filePath = "frontend" + (path.startsWith("/html/") ? path : "/html" + path);
        } else if (path.startsWith("/css/") || path.endsWith(".css")) {
            filePath = "frontend" + (path.startsWith("/css/") ? path : "/css" + path);
        } else if (path.startsWith("/js/") || path.endsWith(".js")) {
            filePath = "frontend" + (path.startsWith("/js/") ? path : "/js" + path);
        } else {
            filePath = "frontend" + path;
        }
        
        File file = new File(filePath);
        if (file.exists() && !file.isDirectory()) {
            String contentType = getContentType(path);
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.sendResponseHeaders(200, file.length());
            
            try (OutputStream os = exchange.getResponseBody();
                 FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, count);
                }
            }
        } else {
            String response = "404 Not Found: " + filePath;
            exchange.sendResponseHeaders(404, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }

    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        return "text/plain";
    }

    // Authentication handlers
    private void handleAuthRegister(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String requestBody = readRequestBody(exchange);
                Map<String, String> data = parseJson(requestBody);
                
                // Create UserDTO
                UserDTO userDTO = new UserDTO();
                userDTO.setUsername(data.get("username"));
                userDTO.setPassword(data.get("password"));
                userDTO.setRole(data.get("role"));
                userDTO.setLinkedEntityId(data.get("linkedEntityId"));
                
                String userId = facade.registerUser(userDTO);
                sendJsonResponse(exchange, 201, "{\"userId\":\"" + userId + "\",\"message\":\"User registered successfully\"}");
            } catch (ValidationException e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private void handleAuthLogin(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String requestBody = readRequestBody(exchange);
                Map<String, String> data = parseJson(requestBody);
                
                // Create LoginRequestDTO
                LoginRequestDTO loginRequest = new LoginRequestDTO();
                loginRequest.setUsername(data.get("username"));
                loginRequest.setPassword(data.get("password"));
                
                LoginResponseDTO response = facade.login(loginRequest);
                
                // Convert to JSON
                String json = String.format("{\"userId\":\"%s\",\"username\":\"%s\",\"role\":\"%s\",\"token\":\"%s\",\"linkedEntityId\":\"%s\"}",
                    response.getUserId(), response.getUsername(), response.getRole(), response.getToken(),
                    response.getLinkedEntityId() != null ? response.getLinkedEntityId() : "");
                
                sendJsonResponse(exchange, 200, json);
            } catch (ValidationException e) {
                sendJsonResponse(exchange, 401, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private void handleAuthValidate(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String requestBody = readRequestBody(exchange);
                Map<String, String> data = parseJson(requestBody);
                
                String token = data.get("token");
                LoginResponseDTO response = facade.validateToken(token);
                
                // Convert to JSON
                String json = String.format("{\"userId\":\"%s\",\"username\":\"%s\",\"role\":\"%s\",\"token\":\"%s\",\"linkedEntityId\":\"%s\"}",
                    response.getUserId(), response.getUsername(), response.getRole(), response.getToken(),
                    response.getLinkedEntityId() != null ? response.getLinkedEntityId() : "");
                
                sendJsonResponse(exchange, 200, json);
            } catch (ValidationException e) {
                sendJsonResponse(exchange, 401, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // Patient handlers
    private void handlePatients(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            List<PatientDTO> patients = facade.listPatients();
            String json = toJson(patients);
            sendJsonResponse(exchange, 200, json);
        } else if ("DELETE".equals(exchange.getRequestMethod())) {
            // Extract patient ID from URL path: /api/patients/{patientId}
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length < 4) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Patient ID is required\"}");
                return;
            }
            
            String patientId = parts[3]; // /api/patients/{patientId}
            
            try {
                // TODO: Add token validation and admin role check
                facade.deletePatient(patientId);
                sendJsonResponse(exchange, 200, "{\"message\":\"Patient deleted successfully\"}");
            } catch (EntityNotFoundException e) {
                sendJsonResponse(exchange, 404, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private void handleRegisterPatient(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String requestBody = readRequestBody(exchange);
                System.out.println("Register Patient Request Body: " + requestBody);
                
                Map<String, String> data = parseJson(requestBody);
                System.out.println("Parsed data: " + data);
                
                // Validate required fields
                if (!data.containsKey("name") || !data.containsKey("age") || 
                    !data.containsKey("gender") || !data.containsKey("contact")) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Missing required fields: name, age, gender, contact\"}");
                    return;
                }
                
                PatientDTO dto = new PatientDTO(
                    null,
                    data.get("name"),
                    Integer.parseInt(data.get("age")),
                    data.get("gender"),
                    data.get("contact")
                );
                
                String patientId = facade.registerPatient(dto);
                System.out.println("Patient registered successfully: " + patientId);
                sendJsonResponse(exchange, 201, "{\"patientId\":\"" + patientId + "\",\"message\":\"Patient registered successfully\"}");
            } catch (ValidationException e) {
                System.err.println("Validation error: " + e.getMessage());
                sendJsonResponse(exchange, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (NumberFormatException e) {
                System.err.println("Number format error: " + e.getMessage());
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid age format\"}");
            } catch (Exception e) {
                System.err.println("Server error: " + e.getMessage());
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"error\":\"Server error: " + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // Doctor handlers
    private void handleDoctors(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            List<DoctorDTO> doctors = facade.listDoctors();
            String json = toJson(doctors);
            sendJsonResponse(exchange, 200, json);
        } else if ("DELETE".equals(exchange.getRequestMethod())) {
            // Extract doctor ID from URL path: /api/doctors/{doctorId}
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length < 4) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Doctor ID is required\"}");
                return;
            }
            
            String doctorId = parts[3]; // /api/doctors/{doctorId}
            
            try {
                // TODO: Add token validation and admin role check
                facade.deleteDoctor(doctorId);
                sendJsonResponse(exchange, 200, "{\"message\":\"Doctor deleted successfully\"}");
            } catch (EntityNotFoundException e) {
                sendJsonResponse(exchange, 404, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private void handleRegisterDoctor(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = readRequestBody(exchange);
            Map<String, String> data = parseJson(requestBody);
            
            try {
                DoctorDTO dto = new DoctorDTO(
                    null,  // ID will be auto-generated
                    data.get("name"),
                    data.get("specialty"),
                    data.get("contact"),
                    data.get("email"),
                    data.get("schedule")
                );
                
                String doctorId = facade.registerDoctor(dto);
                sendJsonResponse(exchange, 201, "{\"doctorId\":\"" + doctorId + "\",\"message\":\"Doctor registered successfully\"}");
            } catch (ValidationException e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // Appointment handlers
    private void handleAppointments(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.startsWith("patientId=")) {
                String patientId = query.split("=")[1];
                List<AppointmentDTO> appointments = facade.getAppointmentsByPatient(patientId);
                sendJsonResponse(exchange, 200, toJson(appointments));
            } else if (query != null && query.startsWith("doctorId=")) {
                String doctorId = query.split("=")[1];
                List<AppointmentDTO> appointments = facade.getAppointmentsByDoctor(doctorId);
                sendJsonResponse(exchange, 200, toJson(appointments));
            } else {
                List<AppointmentDTO> appointments = facade.listAppointments();
                sendJsonResponse(exchange, 200, toJson(appointments));
            }
        } else if ("DELETE".equals(exchange.getRequestMethod())) {
            // Extract appointment ID from URL path: /api/appointments/{appointmentId}
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length < 4) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Appointment ID is required\"}");
                return;
            }
            
            String appointmentId = parts[3]; // /api/appointments/{appointmentId}
            
            try {
                // TODO: Add token validation and admin role check
                facade.deleteAppointment(appointmentId);
                sendJsonResponse(exchange, 200, "{\"message\":\"Appointment deleted successfully\"}");
            } catch (EntityNotFoundException e) {
                sendJsonResponse(exchange, 404, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private void handleBookAppointment(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = readRequestBody(exchange);
            System.out.println("Book Appointment Request Body: " + requestBody);
            Map<String, String> data = parseJson(requestBody);
            
            try {
                String patientId = data.get("patientId");
                String doctorId = data.get("doctorId");
                String dateTimeStr = data.get("dateTime");
                String reason = data.get("reason");
                
                System.out.println("Parsed data - PatientID: " + patientId + ", DoctorID: " + doctorId + ", DateTime: " + dateTimeStr + ", Reason: " + reason);
                
                // Handle datetime-local format (YYYY-MM-DDTHH:MM)
                LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr);
                
                AppointmentDTO appointment = facade.bookAppointment(patientId, doctorId, dateTime, reason);
                
                sendJsonResponse(exchange, 201, "{\"appointmentId\":\"" + appointment.getAppointmentId() + "\",\"message\":\"Appointment booked successfully\"}");
            } catch (EntityNotFoundException | SlotUnavailableException | ValidationException e) {
                System.err.println("Booking error (400): " + e.getMessage());
                e.printStackTrace();
                sendJsonResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
            } catch (Exception e) {
                System.err.println("Booking error (500): " + e.getMessage());
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"error\":\"Invalid date/time format or server error: " + e.getMessage() + "\"}");
            }
        }
    }

    private void handleCancelAppointment(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = readRequestBody(exchange);
            Map<String, String> data = parseJson(requestBody);
            
            try {
                String appointmentId = data.get("appointmentId");
                boolean cancelled = facade.cancelAppointment(appointmentId);
                sendJsonResponse(exchange, 200, "{\"success\":" + cancelled + ",\"message\":\"Appointment cancelled\"}");
            } catch (EntityNotFoundException e) {
                sendJsonResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // Health Record handlers
    private void handleHealthRecords(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.startsWith("patientId=")) {
                String patientId = query.split("=")[1];
                List<HealthRecordDTO> records = facade.getPatientHealthRecords(patientId);
                sendJsonResponse(exchange, 200, toJson(records));
            } else {
                List<HealthRecordDTO> records = facade.listAllHealthRecords();
                sendJsonResponse(exchange, 200, toJson(records));
            }
        } else if ("DELETE".equals(exchange.getRequestMethod())) {
            // Extract health record ID from URL path: /api/health-records/{recordId}
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length < 4) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Health record ID is required\"}");
                return;
            }
            
            String recordId = parts[3]; // /api/health-records/{recordId}
            
            try {
                // TODO: Add token validation and admin role check
                facade.deleteHealthRecord(recordId);
                sendJsonResponse(exchange, 200, "{\"message\":\"Health record deleted successfully\"}");
            } catch (EntityNotFoundException e) {
                sendJsonResponse(exchange, 404, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private void handleAddHealthRecord(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = readRequestBody(exchange);
            Map<String, String> data = parseJson(requestBody);
            
            try {
                String recordDateStr = data.get("recordDate");
                LocalDateTime recordDate = (recordDateStr != null && !recordDateStr.isEmpty()) 
                    ? LocalDateTime.parse(recordDateStr + "T00:00:00") 
                    : LocalDateTime.now();
                
                HealthRecordDTO dto = new HealthRecordDTO(
                    data.get("patientId"),
                    data.get("doctorId"),
                    recordDate,
                    data.get("symptoms"),
                    data.get("diagnosis"),
                    data.get("treatment"),
                    data.get("prescription")
                );
                
                String recordId = facade.addHealthRecord(dto);
                sendJsonResponse(exchange, 201, "{\"recordId\":\"" + recordId + "\",\"message\":\"Health record added successfully\"}");
            } catch (EntityNotFoundException | ValidationException e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"Server error: " + e.getMessage() + "\"}");
            }
        }
    }

    // Utility methods
    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        
        // Match "key":"value" patterns (for strings)
        java.util.regex.Pattern stringPattern = java.util.regex.Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");
        java.util.regex.Matcher stringMatcher = stringPattern.matcher(json);
        
        while (stringMatcher.find()) {
            String key = stringMatcher.group(1);
            String value = stringMatcher.group(2);
            map.put(key, value);
        }
        
        // Match "key":number patterns (for numbers and booleans)
        java.util.regex.Pattern numberPattern = java.util.regex.Pattern.compile("\"([^\"]+)\"\\s*:\\s*([0-9.]+|true|false|null)");
        java.util.regex.Matcher numberMatcher = numberPattern.matcher(json);
        
        while (numberMatcher.find()) {
            String key = numberMatcher.group(1);
            String value = numberMatcher.group(2);
            if (!map.containsKey(key)) { // Don't overwrite string values
                map.put(key, value);
            }
        }
        
        return map;
    }

    private String toJson(Object obj) {
        // Simple JSON serialization (for demo purposes)
        // In production, use Jackson or Gson
        if (obj instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(objectToJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        return objectToJson(obj);
    }

    private String objectToJson(Object obj) {
        if (obj instanceof PatientDTO) {
            PatientDTO p = (PatientDTO) obj;
            return String.format("{\"patientId\":\"%s\",\"name\":\"%s\",\"age\":%d,\"gender\":\"%s\",\"contact\":\"%s\"}",
                p.getPatientId(), p.getName(), p.getAge(), p.getGender(), p.getContact());
        } else if (obj instanceof DoctorDTO) {
            DoctorDTO d = (DoctorDTO) obj;
            return String.format("{\"doctorId\":\"%s\",\"name\":\"%s\",\"specialty\":\"%s\",\"availableSlots\":%d}",
                d.getDoctorId(), d.getName(), d.getSpecialty(), d.getAvailableSlots().size());
        } else if (obj instanceof AppointmentDTO) {
            AppointmentDTO a = (AppointmentDTO) obj;
            return String.format("{\"appointmentId\":\"%s\",\"patientId\":\"%s\",\"doctorId\":\"%s\",\"dateTime\":\"%s\",\"status\":\"%s\"}",
                a.getAppointmentId(), a.getPatientId(), a.getDoctorId(), a.getDateTime().format(formatter), a.getStatus());
        } else if (obj instanceof HealthRecordDTO) {
            HealthRecordDTO h = (HealthRecordDTO) obj;
            return String.format("{\"recordId\":\"%s\",\"patientId\":\"%s\",\"doctorId\":\"%s\",\"date\":\"%s\",\"symptoms\":\"%s\",\"diagnosis\":\"%s\",\"prescription\":\"%s\"}",
                h.getRecordId(), h.getPatientId(), h.getDoctorId(), h.getDate().format(formatter), 
                h.getSymptoms(), h.getDiagnosis(), h.getPrescription());
        }
        return "{}";
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    public void start() {
        server.setExecutor(null);
        server.start();
        System.out.println("========================================");
        System.out.println("  Digital Health Repository API Server");
        System.out.println("========================================");
        System.out.println("Server started on http://localhost:" + PORT);
        System.out.println("Frontend: http://localhost:" + PORT);
        System.out.println("API Base: http://localhost:" + PORT + "/api");
        System.out.println("========================================\n");
    }

    public void stop() {
        server.stop(0);
        System.out.println("Server stopped.");
    }

    public static void main(String[] args) {
        try {
            ApiServer server = new ApiServer();
            server.start();
            
            // Keep server running
            System.out.println("Press Ctrl+C to stop the server...\n");
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
