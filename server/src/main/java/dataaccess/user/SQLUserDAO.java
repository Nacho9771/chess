package dataaccess;

import model.UserData;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        configureDatabase();
    }

    public void createUser(UserData userData) throws DataAccessException {
    }

    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    public void clear() throws DataAccessException {
    }

    private void configureDatabase() throws DataAccessException {
    }
}
