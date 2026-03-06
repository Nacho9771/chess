package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import service.create.CreateGameRequest;
import service.create.CreateGameResult;
import service.join.JoinGameRequest;
import service.list.GameListEntry;
import service.list.ListGamesResult;
import java.util.ArrayList;

public class GameService {

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public ListGamesResult listGames(String token) throws ServiceException, DataAccessException {
        ServiceUtil.requireAuth(token, authDAO);
        var entries = new ArrayList<GameListEntry>();
        for (GameData game : gameDAO.listGames()) {
            entries.add(new GameListEntry(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
        }

        return new ListGamesResult(entries);
    }

    public CreateGameResult createGame(
            String token,
            CreateGameRequest req
    ) throws ServiceException, DataAccessException {
        ServiceUtil.requireAuth(token, authDAO);
        validateCreate(req);
        int id = gameDAO.createGame(
                new GameData(0, null, null, req.gameName(), new ChessGame())
        );
        return new CreateGameResult(id);
    }

    public void joinGame(String token, JoinGameRequest req) throws ServiceException, DataAccessException {
        AuthData auth = ServiceUtil.requireAuth(token, authDAO);
        validateJoin(req);
        GameData game = requireGame(req.gameID());
        String white = game.whiteUsername();
        String black = game.blackUsername();

        // Enforce a single player per color.
        if ("WHITE".equals(req.playerColor())) {
            if (white != null) {
                throw ServiceUtil.alreadyTaken();
            }
            white = auth.username();
        } else {
            if (black != null) {
                throw ServiceUtil.alreadyTaken();
            }
            black = auth.username();
        }

        gameDAO.updateGame(new GameData(game.gameID(), white, black, game.gameName(), game.game()));
    }

    private void validateCreate(CreateGameRequest r) throws ServiceException {
        if (r == null || ServiceUtil.isBlank(r.gameName())) {
            throw ServiceUtil.badRequest();
        }
    }

    private void validateJoin(JoinGameRequest r) throws ServiceException {
        if (r == null || r.gameID() == null || !isValidColor(r.playerColor())) {
            throw ServiceUtil.badRequest();
        }
    }

    private GameData requireGame(Integer id) throws ServiceException, DataAccessException {
        GameData game = gameDAO.getGame(id);
        if (game == null) {
            throw ServiceUtil.badRequest();
        }
        return game;
    }

    private boolean isValidColor(String color) {
        return "WHITE".equals(color) || "BLACK".equals(color);
    }
}
