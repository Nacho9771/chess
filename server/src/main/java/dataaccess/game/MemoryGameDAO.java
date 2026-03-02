package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {

    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public int createGame(GameData gameData) throws DataAccessException {
        if (gameData == null || gameData.gameName() == null) {
            throw new DataAccessException("Invalid game");
        }
        int gameID = nextGameID++;
        GameData storedGame = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(),
                gameData.gameName(), gameData.game() == null ? new ChessGame() : gameData.game());
        games.put(gameID, storedGame);
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public void updateGame(GameData newGameData) throws DataAccessException {
        if (newGameData == null || !games.containsKey(newGameData.gameID())) {
            throw new DataAccessException("Game does not exist");
        }
        games.put(newGameData.gameID(), newGameData);
    }

    public Collection<GameData> listGames() {
        return games.values();
    }

    @Override
    public void clear() {
        games.clear();
        nextGameID = 1;
    }

}
