package storage;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Chaveiro {
    private static final String DATABASE_URL = "jdbc:sqlite:CofreDigital.db";
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/chaveiro/add", Chaveiro::AddChaveiro);
        server.createContext("/api/chaveiro/get", Chaveiro::GetChaveiros);
        server.createContext("/api/chaveiro/update", Chaveiro::UpdateChaveiro);
        server.createContext("/api/chaveiro/delete", Chaveiro::DeleteChaveiro);
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + PORT);
    }

    public static void AddChaveiro(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(requestBody, JsonObject.class);

                int uid = json.get("UID").getAsInt();
                byte[] certificadoDigital = json.get("certificado_digital").getAsString().getBytes();
                byte[] chavePrivada = json.get("chave_privada").getAsString().getBytes();

                String sql = "INSERT INTO Chaveiro (UID, certificado_digital, chave_privada) VALUES (?, ?, ?)";
                try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                        PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, uid);
                    pstmt.setBytes(2, certificadoDigital);
                    pstmt.setBytes(3, chavePrivada);
                    pstmt.executeUpdate();
                }

                String response = "Chaveiro added successfully";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
                String response = "Error adding chaveiro";
                exchange.sendResponseHeaders(500, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method not allowed
        }
    }

    public static void GetChaveiros(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                StringBuilder result = new StringBuilder();
                String sql = "SELECT * FROM Chaveiro";

                try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        ResultSet rs = pstmt.executeQuery()) {

                    while (rs.next()) {
                        int kid = rs.getInt("KID");
                        int uid = rs.getInt("UID");
                        byte[] certificadoDigital = rs.getBytes("certificado_digital");
                        byte[] chavePrivada = rs.getBytes("chave_privada");

                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("KID", kid);
                        jsonObject.addProperty("UID", uid);
                        jsonObject.addProperty("certificado_digital", new String(certificadoDigital));
                        jsonObject.addProperty("chave_privada", new String(chavePrivada));

                        result.append(jsonObject.toString()).append("\n");
                    }
                }

                String response = result.toString();
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
                String response = "Error fetching chaveiros";
                exchange.sendResponseHeaders(500, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method not allowed
        }
    }

    public static void UpdateChaveiro(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(requestBody, JsonObject.class);

                int kid = json.get("KID").getAsInt(); // ID do chaveiro a ser atualizado
                int uid = json.get("UID").getAsInt();
                byte[] certificadoDigital = json.get("certificado_digital").getAsString().getBytes();
                byte[] chavePrivada = json.get("chave_privada").getAsString().getBytes();

                String sql = "UPDATE Chaveiro SET UID = ?, certificado_digital = ?, chave_privada = ? WHERE KID = ?";
                try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                        PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, uid);
                    pstmt.setBytes(2, certificadoDigital);
                    pstmt.setBytes(3, chavePrivada);
                    pstmt.setInt(4, kid);
                    pstmt.executeUpdate();
                }

                String response = "Chaveiro updated successfully";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
                String response = "Error updating chaveiro";
                exchange.sendResponseHeaders(500, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method not allowed
        }
    }

    public static void DeleteChaveiro(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(requestBody, JsonObject.class);

                int kid = json.get("KID").getAsInt(); // ID do chaveiro a ser excluído

                String sql = "DELETE FROM Chaveiro WHERE KID = ?";
                try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                        PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, kid);
                    pstmt.executeUpdate();
                }

                String response = "Chaveiro deleted successfully";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
                String response = "Error deleting chaveiro";
                exchange.sendResponseHeaders(500, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method not allowed
        }
    }
}
