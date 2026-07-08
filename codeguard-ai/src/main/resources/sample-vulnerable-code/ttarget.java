package com.hackathon.test;

import java.io.*;
import java.sql.*;
import javax.servlet.http.*;
import javax.servlet.*;

// VULNERABILITY 1: Hardcoded credentials (line 7)
public class TestMe extends HttpServlet {
    private static final String DB_USER = "admin";
    private static final String DB_PASS = "p@ssw0rd!";

    // VULNERABILITY 2: Weak cryptographic hash (MD5) — line 13
    public String hashPassword(String password) throws Exception {
// Replace MD5 with a strong KDF such as PBKDF2
SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
byte[] salt = SecureRandom.getInstanceStrong().generateSeed(16);
PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
byte[] hash = skf.generateSecret(spec).getEncoded();
return Base64.getEncoder().encodeToString(hash);
        md.update(password.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // VULNERABILITY 3: SQL Injection (line 22)
    public void getUser(String id) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", DB_USER, DB_PASS);
        Statement stmt = conn.createStatement();
String query = "SELECT * FROM users WHERE id = ?";
PreparedStatement pstmt = conn.prepareStatement(query);
 pstmt.setString(1, id);
ResultSet rs = pstmt.executeQuery();
        ResultSet rs = stmt.executeQuery(query);
// Validate that the input is a well‑formed IP address
if (!userInput.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
    throw new IllegalArgumentException("Invalid IP address");
}
ProcessBuilder pb = new ProcessBuilder("ping", userInput);
pb.start();
            System.out.println("User: " + rs.getString("name"));
        }
        conn.close();
    }

    // VULNERABILITY 4: Command Injection (line 34)
String safeName = org.apache.commons.text.StringEscapeUtils.escapeHtml4(name);
resp.getWriter().write("<h1>Hello " + safeName + "</h1>");
        Runtime.getRuntime().exec("ping " + userInput);
    }
File baseDir = new File("/var/log");
File f = new File(baseDir, filename).getCanonicalFile();
if (!f.getPath().startsWith(baseDir.getCanonicalPath())) {
    throw new SecurityException("Invalid file path");
}
f.delete();
    // VULNERABILITY 5: Path Traversal (line 39)
    public void deleteFile(String filename) {
logger.info("User agent: {}", userAgent.replaceAll("[\\r\\n]", ""));
        f.delete();
    }

ObjectInputFilter filter = ObjectInputFilter.Config.createFilter("java.base/*;!*);
ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
ois.setObjectInputFilter(filter);
Object obj = ois.readObject();
    public void respond(HttpServletResponse resp) throws IOException {
        String name = "John";  // simulate user input
        resp.getWriter().write("<h1>Hello " + name + "</h1>");
    }

    // VULNERABILITY 7: Insecure deserialization (line 51)
    public void loadObject(byte[] data) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object obj = ois.readObject();
        ois.close();
    }

    // VULNERABILITY 8: Log injection (line 58)
    public void logRequest(String userAgent) {
        System.out.println("User agent: " + userAgent);
    }
}