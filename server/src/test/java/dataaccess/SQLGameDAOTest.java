package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SQLGameDAOTest {

    private SQLGameDAO gameDAO;

    @BeforeEach
    void setup() throws Exception {
        gameDAO = new SQLGameDAO();
        new SQLUserDAO().clear();
        new SQLAuthDAO().clear();
        gameDAO.clear();
    }

    @Test
    void createGamePositive() throws Exception {
        int gameId = gameDAO.createGame(new GameData(0, null, null, "g1", new ChessGame()));
        assertTrue(gameId > 0);

        GameData stored = gameDAO.getGame(gameId);
        assertNotNull(stored);
        assertEquals("g1", stored.gameName());
    }

    @Test
    void createGameNegative() {
        assertThrows(DataAccessException.class,
                () -> gameDAO.createGame(new GameData(0, null, null, null, new ChessGame())));
    }

    @Test
    void getGameNegative() throws Exception {
        assertNull(gameDAO.getGame(9999));
    }

    @Test
    void updateGamePositive() throws Exception {
        int gameId = gameDAO.createGame(new GameData(0, null, null, "g1", new ChessGame()));
        GameData updated = new GameData(gameId, "white", "black", "g1", new ChessGame());

        gameDAO.updateGame(updated);
        GameData stored = gameDAO.getGame(gameId);

        assertEquals("white", stored.whiteUsername());
        assertEquals("black", stored.blackUsername());
    }

    @Test
    void updateGameNegative() {
        GameData updated = new GameData(9999, "white", "black", "g1", new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(updated));
    }

    @Test
    void listGamesPositive() throws Exception {
        gameDAO.createGame(new GameData(0, null, null, "g1", new ChessGame()));
        gameDAO.createGame(new GameData(0, null, null, "g2", new ChessGame()));

        Collection<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());
    }

    @Test
    void clearPositive() throws Exception {
        gameDAO.createGame(new GameData(0, null, null, "g1", new ChessGame()));
        gameDAO.clear();

        assertEquals(0, gameDAO.listGames().size());
    }
}
