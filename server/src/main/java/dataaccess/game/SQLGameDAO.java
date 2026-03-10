package dataaccess;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

public class SQLGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    public SQLGameDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        configureDatabase();
    }

    public int createGame(GameData gameData) throws DataAccessException {
        if (gameData == null || gameData.gameName() == null) {
            throw new DataAccessException("Invalid game");
        }

        var game = gameData.game() == null ? new ChessGame() : gameData.game();
        var statement = "INSERT INTO game (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, gameData.whiteUsername());
            ps.setString(2, gameData.blackUsername());
            ps.setString(3, gameData.gameName());
            ps.setString(4, gson.toJson(game, ChessGame.class));
            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to create game", ex);
        }

        throw new DataAccessException("Error: game ID was not generated");
    }

    public GameData getGame(int gameID) throws DataAccessException {
        var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    var game = gson.fromJson(rs.getString("game"), ChessGame.class);
                    return new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            game
                    );
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to read game", ex);
        }

        return null;
    }

    public void updateGame(GameData newGameData) throws DataAccessException {
        if (newGameData == null) {
            throw new DataAccessException("Invalid game");
        }

        var statement = "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";
        var json = gson.toJson(newGameData.game(), ChessGame.class);

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, newGameData.whiteUsername());
            ps.setString(2, newGameData.blackUsername());
            ps.setString(3, newGameData.gameName());
            ps.setString(4, json);
            ps.setInt(5, newGameData.gameID());

            var rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Game does not exist");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to update game", ex);
        }
    }

    public Collection<GameData> listGames() throws DataAccessException {
        var games = new ArrayList<GameData>();
        var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game ORDER BY gameID";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                games.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        gson.fromJson(rs.getString("game"), ChessGame.class)
                ));
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to list games", ex);
        }

        return games;
    }

    public void clear() throws DataAccessException {
        var statement = "TRUNCATE TABLE game";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to clear games", ex);
        }
    }

    private void configureDatabase() throws DataAccessException {
        var createGameTable = """
                CREATE TABLE IF NOT EXISTS game (
                  gameID INT NOT NULL AUTO_INCREMENT,
                  gameName VARCHAR(256) NOT NULL,
                  whiteUsername VARCHAR(256) DEFAULT NULL,
                  blackUsername VARCHAR(256) DEFAULT NULL,
                  game LONGTEXT NOT NULL,
                  PRIMARY KEY (gameID),
                  INDEX (gameName),
                  INDEX (whiteUsername),
                  INDEX (blackUsername)
                )
                """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(createGameTable)) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to configure game table", ex);
        }
    }
}
