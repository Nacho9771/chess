package dataaccess;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import chess.ChessGame;
import model.GameData;

public class MemoryGameDAO implements GameDAO {

    // In-memory game store with a simple incrementing ID.
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameId = 1;

    @Override
    public int createGame(GameData gameData) throws DataAccessException {

        if (gameData == null || gameData.gameName() == null) {
            throw new DataAccessException("Invalid game");
        }

        int gameId = nextGameId++;
        ChessGame game = gameData.game() == null ? new ChessGame() : gameData.game();

        GameData storedGame = new GameData(
                gameId,
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );

        games.put(gameId, storedGame);
        return gameId;
    }

    @Override
    public GameData getGame(int gameId) {
        return games.get(gameId);
    }

    @Override
    public void updateGame(GameData newGameData) throws DataAccessException {

        if (newGameData == null || !games.containsKey(newGameData.gameID())) {
            throw new DataAccessException("Game does not exist");
        }

        games.put(newGameData.gameID(), newGameData);
    }

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }

    @Override
    public void clearPlayer(String username) {
        if (username == null || username.isBlank()) {
            return;
        }

        for (var entry : new java.util.ArrayList<>(games.entrySet())) {
            GameData game = entry.getValue();
            String white = username.equals(game.whiteUsername()) ? null : game.whiteUsername();
            String black = username.equals(game.blackUsername()) ? null : game.blackUsername();

            if (!Objects.equals(white, game.whiteUsername()) || !Objects.equals(black, game.blackUsername())) {
                games.put(entry.getKey(), new GameData(
                        game.gameID(),
                        white,
                        black,
                        game.gameName(),
                        game.game()
                ));
            }
        }
    }

    @Override
    public void clear() {
        games.clear();
        nextGameId = 1;
    }
}
