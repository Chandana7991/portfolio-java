import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * PortfolioServer
 * A dependency-free Java backend (built on com.sun.net.httpserver) that:
 *  - Serves the static portfolio site (HTML/CSS/JS) from /public
 *  - Exposes REST API endpoints under /api/* backed by simple flat-file storage
 *
 * No Maven / Spring Boot required — compiles and runs with just the JDK.
 *
 * Run:
 *   javac -d out src/*.java
 *   java -cp out PortfolioServer
 * Then open http://localhost:8080
 */
public class PortfolioServer {

    static final Path DATA_DIR = Paths.get("data");
    static final Path CONTACTS_FILE = DATA_DIR.resolve("contacts.jsonl");
    static final Path PUBLIC_DIR = Paths.get("public");

    public static void main(String[] args) throws IOException {
        Files.createDirectories(DATA_DIR);
        if (!Files.exists(CONTACTS_FILE)) {
            Files.createFile(CONTACTS_FILE);
        }

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // ---- API routes ----
        server.createContext("/api/projects", new ProjectsHandler());
        server.createContext("/api/skills", new SkillsHandler());
        server.createContext("/api/internships", new InternshipsHandler());
        server.createContext("/api/certifications", new CertificationsHandler());
        server.createContext("/api/contact", new ContactHandler());

        // ---- Static file serving (catch-all) ----
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(Executors_newFixedThreadPool());
        server.start();
        System.out.println("Portfolio server running at http://localhost:" + port);
    }

    // Small helper so we don't need java.util.concurrent.Executors import clutter above
    private static java.util.concurrent.ExecutorService Executors_newFixedThreadPool() {
        return java.util.concurrent.Executors.newFixedThreadPool(8);
    }

    // ---------------------------------------------------------------
    // Static file handler — serves everything under /public
    // ---------------------------------------------------------------
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            Path filePath = PUBLIC_DIR.resolve(path.substring(1)).normalize();

            // Prevent path traversal outside /public
            if (!filePath.startsWith(PUBLIC_DIR) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
                byte[] notFound = "404 - Not Found".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, notFound.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(notFound); }
                return;
            }

            String contentType = guessContentType(filePath.toString());
            byte[] bytes = Files.readAllBytes(filePath);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        }

        private String guessContentType(String fileName) {
            if (fileName.endsWith(".html")) return "text/html; charset=utf-8";
            if (fileName.endsWith(".css")) return "text/css; charset=utf-8";
            if (fileName.endsWith(".js")) return "application/javascript; charset=utf-8";
            if (fileName.endsWith(".json")) return "application/json; charset=utf-8";
            if (fileName.endsWith(".svg")) return "image/svg+xml";
            if (fileName.endsWith(".png")) return "image/png";
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
            return "application/octet-stream";
        }
    }

    // ---------------------------------------------------------------
    // GET /api/projects  -> JSON array of project data
    // ---------------------------------------------------------------
    static class ProjectsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Json.sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            List<Map<String, String>> projects = new ArrayList<>();

            projects.add(project(
                "TravelGo",
                "Cloud-powered real-time travel booking platform with failover-ready architecture on AWS EC2 and DynamoDB, mutex-based concurrency control, and SNS-driven booking alerts.",
                "Flask, React.js, MongoDB, AWS (EC2, DynamoDB, SNS)",
                "https://github.com/chandana1771"
            ));
            projects.add(project(
                "Flood Forecasting System",
                "Hybrid CNN-LSTM deep learning model that predicts flood risk from historical hydrological and meteorological data.",
                "Python, TensorFlow/Keras, NumPy, Pandas",
                "https://github.com/chandana1771"
            ));
            projects.add(project(
                "Supermarket Sales Analysis",
                "Tableau dashboards analyzing 1,000+ transactions across 3 branches to surface sales and operational trends.",
                "Tableau",
                "https://github.com/chandana1771"
            ));

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < projects.size(); i++) {
                json.append(Json.toJsonObject(projects.get(i)));
                if (i < projects.size() - 1) json.append(",");
            }
            json.append("]");

            Json.sendJson(exchange, 200, json.toString());
        }

        private Map<String, String> project(String title, String desc, String tech, String link) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("title", title);
            m.put("description", desc);
            m.put("tech", tech);
            m.put("link", link);
            return m;
        }
    }

    // ---------------------------------------------------------------
    // GET /api/skills -> JSON array of skill categories
    // ---------------------------------------------------------------
    static class SkillsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Json.sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            LinkedHashMap<String, String> skills = new LinkedHashMap<>();
            skills.put("Languages", "Java, Python, SQL, JavaScript");
            skills.put("Web & Frameworks", "Flask, React.js, REST APIs");
            skills.put("Data & Analytics", "Pandas, NumPy, Tableau, Excel");
            skills.put("Cloud", "AWS (EC2, DynamoDB, SNS) — AWS Certified Cloud Practitioner");
            skills.put("Tools", "Git/GitHub, VS Code, Jupyter");

            StringBuilder json = new StringBuilder("[");
            int i = 0;
            for (Map.Entry<String, String> e : skills.entrySet()) {
                Map<String, String> m = new LinkedHashMap<>();
                m.put("category", e.getKey());
                m.put("items", e.getValue());
                json.append(Json.toJsonObject(m));
                if (i < skills.size() - 1) json.append(",");
                i++;
            }
            json.append("]");

            Json.sendJson(exchange, 200, json.toString());
        }
    }

    // ---------------------------------------------------------------
    // GET /api/internships -> JSON array of internship data
    // ---------------------------------------------------------------
    static class InternshipsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Json.sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            List<Map<String, String>> internships = new ArrayList<>();

            internships.add(internship(
                "Data Analytics with Tableau",
                "SmartBridge Educational Services Pvt. Ltd., in collaboration with APSCHE",
                "2-month short-term internship · Aug 2025",
                "Short-term internship in data analytics using Tableau, organized by SmartBridge Educational Services in collaboration with the Andhra Pradesh State Council of Higher Education.",
                "Cert ID: EXT-APSCHE_DA-55726"
            ));
            internships.add(internship(
                "Networking Virtual Internship",
                "AICTE EduSkills / National Internship Portal, supported by Zscaler",
                "10 weeks · Jul \u2013 Sep 2025",
                "Virtual internship in networking under the AICTE EduSkills National Internship Portal program, supported by Zscaler. Completed with an Outstanding (O) grade.",
                "Cert ID: 0a8ce46d583bf346ce1bfa79ace35532"
            ));

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < internships.size(); i++) {
                json.append(Json.toJsonObject(internships.get(i)));
                if (i < internships.size() - 1) json.append(",");
            }
            json.append("]");

            Json.sendJson(exchange, 200, json.toString());
        }

        private Map<String, String> internship(String title, String org, String duration, String desc, String credential) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("title", title);
            m.put("org", org);
            m.put("duration", duration);
            m.put("description", desc);
            m.put("credential", credential);
            return m;
        }
    }

    // ---------------------------------------------------------------
    // GET /api/certifications -> JSON array of certification data
    // ---------------------------------------------------------------
    static class CertificationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Json.sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            List<Map<String, String>> certs = new ArrayList<>();

            certs.add(cert(
                "AWS Certified Cloud Practitioner",
                "Amazon Web Services",
                "Issued Oct 22, 2025 \u00b7 Valid through Oct 22, 2028",
                "Validation Number: b6aac369fd604233abd1ef1049a8997c",
                "https://aws.amazon.com/verification"
            ));
            certs.add(cert(
                "Data Visualisation: Empowering Business with Effective Insights",
                "Tata (via Forage)",
                "Aug 3, 2025",
                "User Verification Code: QZ4uxN6pG4cNtcNd6",
                ""
            ));
            certs.add(cert(
                "Introduction to Cloud Infrastructure: Describe Cloud Concepts",
                "Microsoft Learn",
                "Jul 15, 2026",
                "",
                ""
            ));
            certs.add(cert(
                "Introduction to Cloud Infrastructure: Describe Azure Management and Governance",
                "Microsoft Learn",
                "Jul 15, 2026",
                "",
                ""
            ));
            certs.add(cert(
                "TCS iON Career Edge \u2014 Young Professional",
                "TCS iON, Tata Consultancy Services",
                "17\u201331 May 2025",
                "Cert ID: 240640-28354361-1016",
                ""
            ));
            certs.add(cert(
                "Linux Essentials",
                "Cisco Networking Academy (NDG)",
                "27 Sep 2025",
                "",
                ""
            ));
            certs.add(cert(
                "Software Testing (Elite, 12-week course)",
                "NPTEL \u00b7 IIT Madras",
                "Jul \u2013 Oct 2024",
                "Roll No: NPTEL24CS91S255800347 \u00b7 Score: 62%",
                ""
            ));
            certs.add(cert(
                "Introduction to Cloud Computing",
                "Infosys Springboard",
                "Oct 3, 2025",
                "",
                ""
            ));
            certs.add(cert(
                "Python Programming (SOC-2)",
                "CodeTantra",
                "Feb \u2013 May 2023",
                "Cert ID: CT219-tNfJwTb-cb9E",
                ""
            ));
            certs.add(cert(
                "Programming for Problem Solving Using C (SOC-1)",
                "CodeTantra",
                "Aug \u2013 Dec 2022",
                "Cert ID: CT219-tNfJwTb",
                ""
            ));

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < certs.size(); i++) {
                json.append(Json.toJsonObject(certs.get(i)));
                if (i < certs.size() - 1) json.append(",");
            }
            json.append("]");

            Json.sendJson(exchange, 200, json.toString());
        }

        private Map<String, String> cert(String title, String issuer, String date, String credential, String verifyUrl) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("title", title);
            m.put("issuer", issuer);
            m.put("date", date);
            m.put("credential", credential);
            m.put("verifyUrl", verifyUrl);
            return m;
        }
    }

    // ---------------------------------------------------------------
    // POST /api/contact -> accepts {name, email, message}, appends to file
    // ---------------------------------------------------------------
    static class ContactHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Json.sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body;
            try (InputStream is = exchange.getRequestBody()) {
                body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            Map<String, String> fields = Json.parseFlatJson(body);
            String name = fields.getOrDefault("name", "").trim();
            String email = fields.getOrDefault("email", "").trim();
            String message = fields.getOrDefault("message", "").trim();

            if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
                Json.sendJson(exchange, 400, "{\"error\":\"name, email, and message are all required\"}");
                return;
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            Map<String, String> record = new LinkedHashMap<>();
            record.put("timestamp", timestamp);
            record.put("name", name);
            record.put("email", email);
            record.put("message", message);

            String line = Json.toJsonObject(record) + System.lineSeparator();
            Files.write(CONTACTS_FILE, line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            Json.sendJson(exchange, 200, "{\"status\":\"ok\",\"message\":\"Thanks for reaching out! I'll get back to you soon.\"}");
        }
    }

    // ---------------------------------------------------------------
    // Minimal JSON helpers (no external dependency)
    // ---------------------------------------------------------------
    static class Json {
        static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        }

        static String toJsonObject(Map<String, String> map) {
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<String, String> e : map.entrySet()) {
                sb.append("\"").append(escape(e.getKey())).append("\":\"").append(escape(e.getValue())).append("\"");
                if (i < map.size() - 1) sb.append(",");
                i++;
            }
            sb.append("}");
            return sb.toString();
        }

        // Very small flat JSON parser: handles {"key":"value", ...} with string values only.
        static Map<String, String> parseFlatJson(String json) {
            Map<String, String> result = new LinkedHashMap<>();
            if (json == null) return result;
            String trimmed = json.trim();
            if (trimmed.startsWith("{")) trimmed = trimmed.substring(1);
            if (trimmed.endsWith("}")) trimmed = trimmed.substring(0, trimmed.length() - 1);

            List<String> pairs = splitTopLevel(trimmed);
            for (String pair : pairs) {
                int colon = findUnescapedColon(pair);
                if (colon < 0) continue;
                String key = unquote(pair.substring(0, colon).trim());
                String value = unquote(pair.substring(colon + 1).trim());
                result.put(key, unescape(value));
            }
            return result;
        }

        private static List<String> splitTopLevel(String s) {
            List<String> parts = new ArrayList<>();
            int depth = 0;
            boolean inString = false;
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) inString = !inString;
                if (!inString) {
                    if (c == '{' || c == '[') depth++;
                    if (c == '}' || c == ']') depth--;
                }
                if (c == ',' && depth == 0 && !inString) {
                    parts.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
            if (current.length() > 0) parts.add(current.toString());
            return parts;
        }

        private static int findUnescapedColon(String s) {
            boolean inString = false;
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) inString = !inString;
                if (c == ':' && !inString) return i;
            }
            return -1;
        }

        private static String unquote(String s) {
            s = s.trim();
            if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
                return s.substring(1, s.length() - 1);
            }
            return s;
        }

        private static String escape(String s) {
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
        }

        private static String unescape(String s) {
            return s.replace("\\\"", "\"").replace("\\n", " ").replace("\\\\", "\\");
        }
    }
}
