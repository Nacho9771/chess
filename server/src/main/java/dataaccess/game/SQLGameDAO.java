package dataaccess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

public class SQLGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    public SQLGameDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        createGameTable();
    }

    // Public Methods

    public int createGame(GameData gameData) throws DataAccessException {

        validateGameData(gameData);

        ChessGame game = gameData.game() == null ? new ChessGame() : gameData.game();
        String gameJson = gson.toJson(game);

        String sql = """
                INSERT INTO game (gameName, whiteUsername, blackUsername, game)
                VALUES (?, ?, ?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setParameter(statement, 1, gameData.gameName());
            setParameter(statement, 2, gameData.whiteUsername());
            setParameter(statement, 3, gameData.blackUsername());
            setParameter(statement, 4, gameJson);

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to create game", e);
        }

        throw new DataAccessException("Game ID was not generated");
    }

    public GameData getGame(int gameID) throws DataAccessException {

        if (gameID <= 0) {
            return null;
        }

        String sql = """
                SELECT gameID, gameName, whiteUsername, blackUsername, game
                FROM game
                WHERE gameID = ?
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setInt(1, gameID);

            try (ResultSet results = statement.executeQuery()) {

                if (results.next()) {
                    return readGameRow(results);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to read game", e);
        }

        return null;
    }

    public void updateGame(GameData newGameData) throws DataAccessException {

    }

    public Collection<GameData> listGames() throws DataAccessException {

        return games;
    }

    public void clear() throws DataAccessException {
        executeUpdate("DELETE FROM game");
    }


    private void createGameTable() throws DataAccessException {

        String sql = """
                CREATE TABLE IF NOT EXISTS game (
                    gameID INT NOT NULL AUTO_INCREMENT,
                    gameName VARCHAR(255) NOT NULL,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    game TEXT NOT NULL,
                    PRIMARY KEY (gameID)
                )
                """;

        executeUpdate(sql);
    }

    // Helper Methods

    /**
     * Converts a SQL row into a GameData object
     */
    private GameData readGameRow(ResultSet results) throws SQLException {

        return new GameData();
    }


    private void executeUpdate(String sql, Object... parameters) throws DataAccessException {

    }


    private void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {

    }


    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }


    private void validateGameData(GameData gameData) throws DataAccessException {

        if (gameData == null ||
                gameData.gameID() < 0 ||
                isBlank(gameData.gameName())) {

            throw new DataAccessException("Invalid game data");
        }
    }
}