package dataaccess;

import model.AuthData;

public interface AuthDAO {

    boolean isAuth(String authToken) throws DataAccessException;

    public void createAuth(AuthData authData) throws DataAccessException;

    public AuthData getAuth(String authToken) throws DataAccessException;

    public void deleteAuth(AuthData authData) throws DataAccessException;

    void clear() throws DataAccessException;
}