package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.create.CreateGameRequest;
import service.create.CreateGameResult;
import service.join.JoinGameRequest;
import service.list.ListGamesResult;
import service.user.RegisterRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameServiceTest {
    private UserService userService;
    private GameService gameService;
    private String authToken;

    @BeforeEach
    void setup() throws Exception {
        var userDAO = new dataaccess.MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(authDAO, gameDAO);
        authToken = userService.register(new RegisterRequest("gamer", "password", "gamingw@gmail.com")).authToken();
    }

    @Test
    void listGamesPositive() throws Exception {
        ListGamesResult result = gameService.listGames(authToken);
        assertNotNull(result.games());
        assertEquals(0, result.games().size());
    }

    @Test
    void listGamesNegative() {
        ServiceException ex = assertThrows(ServiceException.class, () -> gameService.listGames("bad-token"));
        assertEquals(401, ex.statusCode());
    }

    @Test
    void createGamePositive() throws Exception {
        CreateGameResult result = gameService.createGame(authToken, new CreateGameRequest("test game"));
        assertNotNull(result.gameID());
        assertTrue(result.gameID() > 0);
    }

    @Test
    void createGameNegative() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> gameService.createGame(authToken, new CreateGameRequest(null)));
        assertEquals(400, ex.statusCode());
    }

    @Test
    void joinGamePositive() throws Exception {
        int gameID = gameService.createGame(authToken, new CreateGameRequest("joinable")).gameID();
        gameService.joinGame(authToken, new JoinGameRequest("WHITE", gameID));
        ListGamesResult result = gameService.listGames(authToken);
        assertEquals("gamer", result.games().getFirst().whiteUsername());
    }

    @Test
    void joinGameNegative() throws Exception {
        int gameID = gameService.createGame(authToken, new CreateGameRequest("joinable")).gameID();
        ServiceException ex = assertThrows(ServiceException.class,
                () -> gameService.joinGame(authToken, new JoinGameRequest("GREEN", gameID)));
        assertEquals(400, ex.statusCode());
    }
}
