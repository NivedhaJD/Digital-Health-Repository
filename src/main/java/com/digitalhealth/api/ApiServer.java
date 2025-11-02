package com.digitalhealth.api;

import com.digitalhealth.dto.*;
import com.digitalhealth.exception.*;
import com.digitalhealth.facade.BackendFacade;
import com.digitalhealth.facade.BackendFactory;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
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
        
        // Patient endpoints
        server.createContext("/api/patients", this::handlePatients);
        server.createContext("/api/patients/register", this::handleRegisterPatient);
        
        // Doctor endpoints
        server.createContext("/api/doctors", this::handleDoctors);
        
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
        if (path.equals("/")) path = "/index.html";
        
        File file = new File("." + path);
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
            String response = "404 Not Found";
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
        }
    }

    private void handleRegisterPatient(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = readRequestBody(exchange);
            Map<String, String> data = parseJson(requestBody);
            
            try {
                PatientDTO dto = new PatientDTO(
                    null,
                    data.get("name"),
                    Integer.parseInt(data.get("age")),
                    data.get("gender"),
                    data.get("contact")
                );
                
                String patientId = facade.registerPatient(dto);
                sendJsonResponse(exchange, 201, "{\"patientId\":\"" + patientId + "\",\"message\":\"Patient registered successfully\"}");
            } catch (ValidationException e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
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
            Map<String, String> data = parseJson(requestBody);
            
            try {
                String patientId = data.get("patientId");
                String doctorId = data.get("doctorId");
                LocalDateTime dateTime = LocalDateTime.parse(data.get("dateTime"), formatter);
                
                AppointmentDTO appointment = facade.bookAppointment(patientId, doctorId, dateTime);
                sendJsonResponse(exchange, 201, "{\"appointmentId\":\"" + appointment.getAppointmentId() + "\",\"message\":\"Appointment booked successfully\"}");
            } catch (EntityNotFoundException | SlotUnavailableException | ValidationException e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
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
                HealthRecordDTO dto = new HealthRecordDTO(
                    data.get("patientId"),
                    data.get("doctorId"),
                    LocalDateTime.now(),
                    data.get("symptoms"),
                    data.get("diagnosis"),
                    data.get("prescription")
                );
                
                String recordId = facade.addHealthRecord(dto);
                sendJsonResponse(exchange, 201, "{\"recordId\":\"" + recordId + "\",\"message\":\"Health record added successfully\"}");
            } catch (EntityNotFoundException | ValidationException e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
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
        json = json.trim().replaceAll("[{}]", "");
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim().replaceAll("\"", "");
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
