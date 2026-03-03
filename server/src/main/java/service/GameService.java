package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;
import service.create.*;
import service.join.*;
import service.list.*;
import java.util.ArrayList;

public class GameService {

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
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
        requireAuth(token);
        validateCreate(req);
        int id = gameDAO.createGame(
                new GameData(0, null, null, req.gameName(), new ChessGame())
        );
        return new CreateGameResult(id);
    }

    public void joinGame(String token, JoinGameRequest req) throws ServiceException, DataAccessException {
        AuthData auth = requireAuth(token);
        validateJoin(req);
        GameData game = requireGame(req.gameID());
        String white = game.whiteUsername();
        String black = game.blackUsername();

        if ("WHITE".equals(req.playerColor())) {
            if (white != null) throw error(403, "Error: already taken");
            white = auth.username();
        } else {
            if (black != null) throw error(403, "Error: already taken");
            black = auth.username();
        }

        gameDAO.updateGame(new GameData(game.gameID(), white, black, game.gameName(), game.game()));
    }

    private void validateCreate(CreateGameRequest r) throws ServiceException {
        if (r == null || isBlank(r.gameName())) {
            throw error(400, "Error: bad request");
        }
    }

    private void validateJoin(JoinGameRequest r) throws ServiceException {
        if (r == null || r.gameID() == null
                || !isValidColor(r.playerColor())) {
            throw error(400, "Error: bad request");
        }
    }

    private GameData requireGame(Integer id) throws ServiceException, DataAccessException {
        GameData game = gameDAO.getGame(id);
        if (game == null) {
            throw error(400, "Error: bad request");
        }
        return game;
    }

    private AuthData requireAuth(String token) throws ServiceException, DataAccessException {
        if (isBlank(token)) {throw error(401, "Error: unauthorized");}

        AuthData auth = authDAO.getAuth(token);
        if (auth == null) {throw error(401, "Error: unauthorized");}

        return auth;
    }

    private boolean isValidColor(String color) {
        return "WHITE".equals(color) || "BLACK".equals(color);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private ServiceException error(int code, String msg) {
        return new ServiceException(code, msg);
    }
}