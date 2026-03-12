package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SQLAuthDAOTest {

    private SQLAuthDAO authDAO;

    @BeforeEach
    void setup() throws Exception {
        authDAO = new SQLAuthDAO();
        new SQLUserDAO().clear();
        new SQLGameDAO().clear();
        authDAO.clear();
    }

    @Test
    void createAuthPositive() throws Exception {
        AuthData auth = new AuthData("token-1", "user1");
        authDAO.createAuth(auth);

        AuthData stored = authDAO.getAuth("token-1");
        assertNotNull(stored);
        assertEquals("user1", stored.username());
    }

    @Test
    void createAuthNegative() throws Exception {
        AuthData auth = new AuthData("token-1", "user1");
        authDAO.createAuth(auth);

        assertThrows(DataAccessException.class, () -> authDAO.createAuth(auth));
    }

    @Test
    void getAuthNegative() throws Exception {
        assertNull(authDAO.getAuth("missing"));
    }

    @Test
    void deleteAuthPositive() throws Exception {
        AuthData auth = new AuthData("token-1", "user1");
        authDAO.createAuth(auth);
        authDAO.deleteAuth("token-1");

        assertNull(authDAO.getAuth("token-1"));
    }

    @Test
    void deleteAuthNegative() {
        assertThrows(DataAccessException.class, () -> authDAO.deleteAuth(null));
    }

    @Test
    void clearPositive() throws Exception {
        authDAO.createAuth(new AuthData("token-1", "user1"));
        authDAO.clear();

        assertNull(authDAO.getAuth("token-1"));
    }
}
