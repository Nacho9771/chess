package server.websocket;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;

// Implements websocket
public final class GameWebSocketHandler {

    private final Gson gson;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final WebSocketHub hub = new WebSocketHub();

    public GameWebSocketHandler(Gson gson, AuthDAO authDAO, GameDAO gameDAO) {
        this.gson = gson;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }
}
