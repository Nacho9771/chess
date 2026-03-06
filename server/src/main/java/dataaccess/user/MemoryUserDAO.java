package dataaccess;

import java.util.HashMap;
import java.util.Map;

import model.UserData;

public class MemoryUserDAO implements UserDAO {

    // In-memory user store for simple use cases and tests.
    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public void createUser(UserData userData) throws DataAccessException {

        if (userData == null || userData.username() == null) {
            throw new DataAccessException("Invalid user");
        }

        if (users.containsKey(userData.username())) {
            throw new DataAccessException("User already exists");
        }

        users.put(userData.username(), userData);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void clear() {
        users.clear();
    }
}
