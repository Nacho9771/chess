package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import org.junit.jupiter.api.Test;
import service.create.CreateGameRequest;
import service.user.RegisterRequest;

import static org.junit.jupiter.api.Assertions.assertNull;

public class ClearServiceTest {
    @Test
    void clearPositive() throws Exception {
        var userDAO = new MemoryUserDAO();
        var authDAO = new MemoryAuthDAO();
        var gameDAO = new MemoryGameDAO();
        var userService = new UserService(userDAO, authDAO);
        var gameService = new GameService(authDAO, gameDAO);
        var clearService = new ClearService(userDAO, authDAO, gameDAO);

        var auth = userService.register(new RegisterRequest("gamer", "password", "wgaming@gmail.com"));
        gameService.createGame(auth.authToken(), new CreateGameRequest("game"));

        clearService.clear();

        assertNull(userDAO.getUser("gamer"));
        assertNull(authDAO.getAuth(auth.authToken()));
    }
}
