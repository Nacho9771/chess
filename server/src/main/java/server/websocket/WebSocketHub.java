package server.websocket;

import io.javalin.websocket.WsContext;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Tracks websocket sessions by game
public final class WebSocketHub {

    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, SessionConnection>> connectionsByGame =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, SessionConnection> connectionIndex = new ConcurrentHashMap<>();

    public void joinGame(int gameId, WsContext ctx, WebSocketConnection connection) {
        leave(ctx);

        SessionConnection sessionConnection = new SessionConnection(ctx, connection);
        String sessionId = ctx.sessionId();
        connectionsByGame
                .computeIfAbsent(gameId, ignored -> new ConcurrentHashMap<>())
                .put(sessionId, sessionConnection);
        connectionIndex.put(sessionId, sessionConnection);
    }

    public void leave(WsContext ctx) {
        String sessionId = ctx.sessionId();
        SessionConnection existing = connectionIndex.remove(sessionId);
        if (existing == null) {
            return;
        }

        Map<String, SessionConnection> gameConnections = connectionsByGame.get(existing.connection().gameId());
        if (gameConnections != null) {
            gameConnections.remove(sessionId);
            if (gameConnections.isEmpty()) {
                connectionsByGame.remove(existing.connection().gameId(), gameConnections);
            }
        }
    }

    public WebSocketConnection get(WsContext ctx) {
        SessionConnection connection = connectionIndex.get(ctx.sessionId());
        return connection == null ? null : connection.connection();
    }

    public ArrayList<WsContext> contextsInGame(int gameId) {
        Map<String, SessionConnection> gameConnections = connectionsByGame.get(gameId);
        if (gameConnections == null || gameConnections.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<WsContext> contexts = new ArrayList<>();
        for (SessionConnection sessionConnection : gameConnections.values()) {
            contexts.add(sessionConnection.ctx());
        }
        return contexts;
    }

    private record SessionConnection(WsContext ctx, WebSocketConnection connection) {
    }
}
