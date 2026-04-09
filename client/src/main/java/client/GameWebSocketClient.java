package client;

import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

public final class GameWebSocketClient extends Endpoint {

    private final Gson gson = new Gson();
    private final String webSocketUrl;
    private final Consumer<LoadGameMessage> loadGameHandler;
    private final Consumer<NotificationMessage> notificationHandler;
    private final Consumer<ErrorMessage> errorMessageHandler;
    private final Consumer<Throwable> errorHandler;
    private final Consumer<String> closeHandler;
    private Session session;

    public GameWebSocketClient(String webSocketUrl,
                               Consumer<LoadGameMessage> loadGameHandler,
                               Consumer<NotificationMessage> notificationHandler,
                               Consumer<ErrorMessage> errorMessageHandler,
                               Consumer<Throwable> errorHandler,
                               Consumer<String> closeHandler) throws ServerFacadeException {
        this.webSocketUrl = webSocketUrl;
        this.loadGameHandler = loadGameHandler;
        this.notificationHandler = notificationHandler;
        this.errorMessageHandler = errorMessageHandler;
        this.errorHandler = errorHandler;
        this.closeHandler = closeHandler;

        connectSocket();
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    private void connectSocket() throws ServerFacadeException {

        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, new URI(webSocketUrl));
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ServerFacadeException("Error: unable to connect to websocket");
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        this.session = session;
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                handleMessage(message);
            }
        });
    }

    public void connect(String authToken, int gameId) throws ServerFacadeException {
        send(new SimpleCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId));
    }

    public void leave(String authToken, int gameId) throws ServerFacadeException {
        send(new SimpleCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId));
    }

    public void resign(String authToken, int gameId) throws ServerFacadeException {
        send(new SimpleCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId));
    }

    public void makeMove(String authToken, int gameId, ChessMove move) throws ServerFacadeException {
        send(new MoveCommand(authToken, gameId, move));
    }

    public void close() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException ignored) {
            }
        }
        session = null;
    }

    private void send(Object command) throws ServerFacadeException {
        if (!isConnected()) {
            connectSocket();
        }
        try {
            session.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException ex) {
            throw new ServerFacadeException("Error: unable to send websocket command");
        }
    }

    private void handleMessage(String message) {
        try {
            JsonObject payload = gson.fromJson(message, JsonObject.class);
            if (payload == null || !payload.has("serverMessageType")) {
                return;
            }

            ServerMessage.ServerMessageType type =
                    ServerMessage.ServerMessageType.valueOf(payload.get("serverMessageType").getAsString());

            switch (type) {
                case LOAD_GAME -> loadGameHandler.accept(gson.fromJson(message, LoadGameMessage.class));
                case NOTIFICATION -> notificationHandler.accept(gson.fromJson(message, NotificationMessage.class));
                case ERROR -> errorMessageHandler.accept(gson.fromJson(message, ErrorMessage.class));
            }
        } catch (Exception ex) {
            errorHandler.accept(ex);
        }
    }

    @Override
    public void onError(Session session, Throwable thr) {
        errorHandler.accept(thr);
    }

    @Override
    public void onClose(Session session, javax.websocket.CloseReason closeReason) {
        this.session = null;
        if (closeHandler != null) {
            closeHandler.accept(closeReason == null ? "closed" : closeReason.toString());
        }
    }

    private record SimpleCommand(UserGameCommand.CommandType commandType, String authToken, Integer gameID) {
    }

    private record MoveCommand(UserGameCommand.CommandType commandType, String authToken, Integer gameID, ChessMove move) {
        private MoveCommand(String authToken, Integer gameID, ChessMove move) {
            this(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move);
        }
    }
}
