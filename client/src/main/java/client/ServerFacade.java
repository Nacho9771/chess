package client;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this("http://localhost:" + port);
    }

    public ServerFacade(String serverUrl) {
        this.serverUrl = normalizeServerUrl(serverUrl);
    }

    private <T> T makeRequest(String method, String path, String authToken, Object requestBody, Class<T> responseClass)
            throws ServerFacadeException {
        HttpURLConnection connection = null;
        try {
            connection = openConnection(method, path, authToken);

            if (requestBody != null) {
                writeBody(connection, requestBody);
            }

            connection.connect();
            handleError(connection);

            if (responseClass == null) {
                return null;
            }

            try (InputStream responseBody = connection.getInputStream()) {
                String json = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                return gson.fromJson(json, responseClass);
            }
        } catch (IOException ex) {
            throw new ServerFacadeException("Error: unable to reach server");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection openConnection(String method, String path, String authToken) throws IOException {
        URL url = URI.create(serverUrl + path).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setDoInput(true);

        if (authToken != null) {
            connection.setRequestProperty("authorization", authToken);
        }

        return connection;
    }

    private void writeBody(HttpURLConnection connection, Object requestBody) throws IOException {
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(gson.toJson(requestBody).getBytes(StandardCharsets.UTF_8));
        }
    }

    private void handleError(HttpURLConnection connection) throws IOException, ServerFacadeException {
        int statusCode = connection.getResponseCode();
        if (statusCode / 100 == 2) {
            return;
        }

        try (InputStream errorStream = connection.getErrorStream()) {
            if (errorStream != null) {
                String json = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                if (errorResponse != null && errorResponse.message() != null && !errorResponse.message().isBlank()) {
                    throw new ServerFacadeException(errorResponse.message());
                }
            }
        }

        throw new ServerFacadeException("Error: request failed");
    }

    private String normalizeServerUrl(String rawUrl) {
        String trimmedUrl = rawUrl == null ? "" : rawUrl.trim();
        if (trimmedUrl.isEmpty()) {
            return "http://localhost:8080";
        }
        return trimmedUrl.endsWith("/") ? trimmedUrl.substring(0, trimmedUrl.length() - 1) : trimmedUrl;
    }

    private record ErrorResponse(String message) { }
}