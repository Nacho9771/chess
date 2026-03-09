package dataaccess;

import model.AuthData;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        configureDatabase();
    }

    public void createAuth(AuthData authData) throws DataAccessException {
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    public void deleteAuth(String authToken) throws DataAccessException {

    }

    public void clear() throws DataAccessException {
    }

    private void configureDatabase() throws DataAccessException {

    }
}
