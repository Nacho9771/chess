package server.websocket;

import io.javalin.websocket.WsContext;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Tracks websocket sessions by game
public final class WebSocketHub {

    private final ConcurrentHashMap<Integer, ConcurrentHashMap<WsContext, WebSocketConnection>>
            connectionsByGame = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<WsContext, WebSocketConnection>
            connectionIndex = new ConcurrentHashMap<>();

    public void joinGame(int gameId, WsContext ctx, WebSocketConnection connection) {
    }

    public void leave(WsContext ctx) {
    }

    public WebSocketConnection get(WsContext ctx) {
        return connectionIndex.get(ctx);
    }

    public ArrayList<WsContext> contextsInGame(int gameId) {
        Map<WsContext, WebSocketConnection> gameConnections = connectionsByGame.get(gameId);
        if (gameConnections == null || gameConnections.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(gameConnections.keySet());
    }
}

