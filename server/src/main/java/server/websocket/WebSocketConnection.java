package server.websocket;

import chess.ChessGame;

// Data for a single websocket session
public record WebSocketConnection(String username, int gameId, Role role, ChessGame.TeamColor color) {

    public enum Role {
        PLAYER,
        OBSERVER
    }
}

