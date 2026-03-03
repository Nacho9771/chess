package service;

import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.user.RegisterRequest;
import service.user.UserService;

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
    }

    @Test
    void loginPositive() throws Exception {
    }

    @Test
    void loginNegative() throws Exception {
    }

    @Test
    void logoutPositive() throws Exception {
    }

    @Test
    void logoutNegative() {
    }
}
