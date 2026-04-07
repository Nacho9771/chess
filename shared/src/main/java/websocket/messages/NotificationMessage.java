package websocket.messages;

/**
 * A server-to-client message that notifies users about game events.
 */
public class NotificationMessage extends ServerMessage {

    private final String message;

    public NotificationMessage(String message) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

