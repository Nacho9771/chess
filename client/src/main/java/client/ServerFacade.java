package client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(int port) {
        this("http://localhost:" + port);
    }

    public ServerFacade(String serverUrl) {
        this.serverUrl = normalizeServerUrl(serverUrl);
    }

    private HttpURLConnection openConnection(String method, String path, String authToken) throws IOException {
        URL url = URI.create(serverUrl + path).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setDoInput(true);

        connection.setRequestProperty("authorization", authToken);

        return connection;
    }

    private void writeBody(HttpURLConnection connection) {
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
    }

    private void handleError(HttpURLConnection connection) throws ServerFacadeException {
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