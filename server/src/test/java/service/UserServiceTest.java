package service;

import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.user.LoginRequest;
import service.user.RegisterRequest;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    void registerPositive() throws Exception {
        AuthResult result = userService.register(new RegisterRequest("gamer", "password", "gamingw@gmail.com"));
        assertEquals("gamer", result.username());
        assertNotNull(result.authToken());
        assertNotNull(userDAO.getUser("gamer"));
    }

    @Test
    void registerNegative() throws Exception {
        userService.register(new RegisterRequest("gamer", "password", "gamingw@gmail.com"));
        ServiceException ex = assertThrows(ServiceException.class,
                () -> userService.register(new RegisterRequest("gamer", "password", "gamingw@gmail.com")));
        assertEquals(403, ex.statusCode());
    }

    @Test
    void loginPositive() throws Exception {
        userService.register(new RegisterRequest("gamer", "password", "gamingw@gmail.com"));
        AuthResult result = userService.login(new LoginRequest("gamer", "password"));
        assertEquals("gamer", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void loginNegative() throws Exception {
        userService.register(new RegisterRequest("gamer", "password", "gamingw@gmail.com"));
        ServiceException ex = assertThrows(ServiceException.class,
                () -> userService.login(new LoginRequest("gamer", "wrong")));
        assertEquals(401, ex.statusCode());
    }

    @Test
    void logoutPositive() throws Exception {
        AuthResult result = userService.register(new RegisterRequest("gamer", "password", "gamingw@gmail.com"));
        userService.logout(result.authToken());
        assertNull(authDAO.getAuth(result.authToken()));
    }

    @Test
    void logoutNegative() {
        ServiceException ex = assertThrows(ServiceException.class, () -> userService.logout("bad-token"));
        assertEquals(401, ex.statusCode());
    }
}
