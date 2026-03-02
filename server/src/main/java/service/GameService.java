package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;
import service.create.*;
import service.join.*;
import service.list.*;
import java.util.ArrayList;

public class GameService {

    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public ListGamesResult listGames(String token) throws ServiceException, DataAccessException {
        requireAuth(token);
        var entries = new ArrayList<GameListEntry>();

        for (GameData game : gameDAO.listGames()) {
            entries.add(new GameListEntry(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
        }

        return new ListGamesResult(entries);
    }

    public CreateGameResult createGame(String token, CreateGameRequest req) throws ServiceException, DataAccessException {

        validateCreate(req);
        int id = gameDAO.createGame(
                new GameData(0, null, null, req.gameName(), new ChessGame())
        );

        return new CreateGameResult(id);
    }

    private void validateCreate(CreateGameRequest r) throws ServiceException {
        if (r == null || isBlank(r.gameName())) {
            throw error(400, "Error: bad request");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private ServiceException error(int code, String msg) {
        return new ServiceException(code, msg);
    }
}