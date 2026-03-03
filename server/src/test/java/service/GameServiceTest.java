package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.list.ListGamesResult;
import service.user.RegisterRequest;
import service.user.UserService;

import static org.junit.jupiter.api.Assertions.*;

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
    void joinGamePositive() throws Exception {
    }

    @Test
    void joinGameNegative() throws Exception {
    }
}
