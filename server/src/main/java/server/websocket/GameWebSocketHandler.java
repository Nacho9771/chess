package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import service.ServiceException;
import service.ServiceUtil;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

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

    public void onMessage(WsMessageContext ctx) {
        UserGameCommandDTO command;
        try {
            command = gson.fromJson(ctx.message(), UserGameCommandDTO.class);
        } catch (Exception ex) {
            sendError(ctx, "Error: bad request");
            return;
        }

        if (command == null || command.commandType == null || command.gameID == null) {
            sendError(ctx, "Error: bad request");
            return;
        }

        try {
            switch (command.commandType) {
                case CONNECT -> handleConnect(ctx, command.authToken, command.gameID);
                case MAKE_MOVE -> handleMove(ctx, command.authToken, command.gameID, command.move);
                case LEAVE -> handleLeave(ctx, command.authToken, command.gameID);
                case RESIGN -> handleResign(ctx, command.authToken, command.gameID);
            }
        } catch (DataAccessException ex) {
            sendError(ctx, "Error: " + ex.getMessage());
        } catch (ServiceException ex) {
            sendError(ctx, ex.getMessage());
        } catch (RuntimeException ex) {
            sendError(ctx, "Error: " + ex.getMessage());
        }
    }

    public void onClose(WsCloseContext ctx) {
        hub.leave(ctx);
    }

    private void handleConnect(WsContext ctx, String token, int gameId) throws ServiceException, DataAccessException {
        AuthData auth = ServiceUtil.requireAuth(token, authDAO);
        GameData gameData = requireGame(gameId);

        WebSocketConnection connection = toConnection(auth.username(), gameData);
        hub.joinGame(gameId, ctx, connection);

        send(ctx, new LoadGameMessage(gameData));
        notifyOthers(gameId, ctx, auth.username() + connectedMessageSuffix(connection));
    }

    private void handleLeave(WsContext ctx, String token, int gameId) throws ServiceException, DataAccessException {
        AuthData auth = ServiceUtil.requireAuth(token, authDAO);
        requireConnectedToGame(ctx, gameId);

        GameData gameData = requireGame(gameId);
        GameData updated = removePlayerIfPresent(gameData, auth.username());
        if (updated != gameData) {
            gameDAO.updateGame(updated);
        }

        hub.leave(ctx);
        notifyAll(gameId, auth.username() + " left the game");
    }

    private void handleResign(WsContext ctx, String token, int gameId) throws ServiceException, DataAccessException {
        AuthData auth = ServiceUtil.requireAuth(token, authDAO);
        requireConnectedToGame(ctx, gameId);

        GameData gameData = requireGame(gameId);
        ChessGame.TeamColor playerColor = playerColor(gameData, auth.username());
        if (playerColor == null) {
            throw new ServiceException(403, "Error: observers cannot resign");
        }

        ChessGame game = gameData.game();
        if (game.isFinished()) {
            throw new ServiceException(403, "Error: game is over");
        }

        game.finishGame();
        GameData updated = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );
        gameDAO.updateGame(updated);

        broadcast(gameId, new NotificationMessage(auth.username() + " resigned"));
    }

    private GameData requireGame(int gameId) throws ServiceException, DataAccessException {
        GameData gameData = gameDAO.getGame(gameId);
        if (gameData == null) {
            throw ServiceUtil.badRequest();
        }
        return gameData;
    }

    private void requireConnectedToGame(WsContext ctx, int gameId) throws ServiceException {
        WebSocketConnection existing = hub.get(ctx);
        if (existing == null || existing.gameId() != gameId) {
            throw new ServiceException(403, "Error: not connected");
        }
    }

    private WebSocketConnection toConnection(String username, GameData gameData) {
        ChessGame.TeamColor color = playerColor(gameData, username);
        if (color == null) {
            return new WebSocketConnection(username, gameData.gameID(), WebSocketConnection.Role.OBSERVER, null);
        }
        return new WebSocketConnection(username, gameData.gameID(), WebSocketConnection.Role.PLAYER, color);
    }

    private ChessGame.TeamColor playerColor(GameData gameData, String username) {
        if (username != null && username.equals(gameData.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        }
        if (username != null && username.equals(gameData.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }
        return null;
    }

    private GameData removePlayerIfPresent(GameData gameData, String username) {
        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();

        boolean changed = false;
        if (username != null && username.equals(white)) {
            white = null;
            changed = true;
        }
        if (username != null && username.equals(black)) {
            black = null;
            changed = true;
        }

        if (!changed) {
            return gameData;
        }

        return new GameData(gameData.gameID(), white, black, gameData.gameName(), gameData.game());
    }

    private String connectedMessageSuffix(WebSocketConnection connection) {
        if (connection.role() == WebSocketConnection.Role.OBSERVER) {
            return " connected as an observer";
        }
        return " connected as " + connection.color();
    }

    private void notifyOthers(int gameId, WsContext sender, String message) {
        NotificationMessage notification = new NotificationMessage(message);
        for (WsContext other : hub.contextsInGame(gameId)) {
            if (other == sender) {
                continue;
            }
            send(other, notification);
        }
    }

    private void notifyAll(int gameId, String message) {
        NotificationMessage notification = new NotificationMessage(message);
        for (WsContext other : hub.contextsInGame(gameId)) {
            send(other, notification);
        }
    }

    private void broadcast(int gameId, Object message) {
        for (WsContext other : hub.contextsInGame(gameId)) {
            send(other, message);
        }
    }

    private void send(WsContext ctx, Object message) {
        ctx.send(gson.toJson(message));
    }

    private void sendError(WsContext ctx, String message) {
        String errorMessage = (message == null || message.isBlank()) ? "Error: unknown error" : message;
        if (!errorMessage.toLowerCase().contains("error")) {
            errorMessage = "Error: " + errorMessage;
        }
        send(ctx, new ErrorMessage(errorMessage));
    }

}
