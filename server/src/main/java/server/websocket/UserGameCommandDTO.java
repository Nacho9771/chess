package server.websocket;

import chess.ChessMove;
import websocket.commands.UserGameCommand;

public final class UserGameCommandDTO {

    public UserGameCommand.CommandType commandType;
    public String authToken;
    public Integer gameID;

    public ChessMove move;
}

