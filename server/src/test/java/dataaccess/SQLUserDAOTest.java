package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SQLUserDAOTest {

    private SQLUserDAO userDAO;

    @BeforeEach
    void setup() throws Exception {
        userDAO = new SQLUserDAO();
        new SQLAuthDAO().clear();
        new SQLGameDAO().clear();
        userDAO.clear();
    }

    @Test
    void createUserPositive() throws Exception {
        UserData user = new UserData("alice", "password", "alice@mail.com");
        userDAO.createUser(user);

        UserData stored = userDAO.getUser("alice");
        assertNotNull(stored);
        assertEquals("alice", stored.username());
        assertEquals("alice@mail.com", stored.email());
        assertNotEquals("password", stored.password());
        assertTrue(BCrypt.checkpw("password", stored.password()));
    }

    @Test
    void createUserNegative() throws Exception {
        UserData user = new UserData("alice", "password", "alice@mail.com");
        userDAO.createUser(user);

        assertThrows(DataAccessException.class, () -> userDAO.createUser(user));
    }

    @Test
    void getUserPositive() throws Exception {
        userDAO.createUser(new UserData("bob", "pw", "bob@mail.com"));
        UserData stored = userDAO.getUser("bob");

        assertNotNull(stored);
        assertEquals("bob", stored.username());
    }

    @Test
    void getUserNegative() throws Exception {
        assertNull(userDAO.getUser("missing"));
    }

    @Test
    void clearPositive() throws Exception {
        userDAO.createUser(new UserData("clear", "pw", "c@mail.com"));
        userDAO.clear();

        assertNull(userDAO.getUser("clear"));
    }
}
