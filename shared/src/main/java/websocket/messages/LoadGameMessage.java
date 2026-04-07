package websocket.messages;

import model.GameData;

/**
 * A server-to-client message that includes the current game state.
 */
public class LoadGameMessage extends ServerMessage {

    private final GameData game;

    public LoadGameMessage(GameData game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }

    public GameData getGame() {
        return game;
    }
}

