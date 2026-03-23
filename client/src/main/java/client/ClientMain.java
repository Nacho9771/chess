package client;

import ui.ChessClient;

public class ClientMain {

    private static final String DEFAULT_SERVER_URL = "http://localhost:8081";

    public static void main(String[] args) {
        String serverUrl = args.length > 0 ? args[0] : DEFAULT_SERVER_URL;
        new ChessClient(new ServerFacade(serverUrl)).run();
    }
}
