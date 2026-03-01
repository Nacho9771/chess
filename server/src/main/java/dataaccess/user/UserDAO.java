package dataaccess;
import dataaccess*;
import model.UserData;

public interface UserDAO {

    boolean isUser(String UserData) throws DataAccessException;

    public void createUser(UserData userData) throws DataAccessException;

    public UserData getUser(String username) throws DataAccessException;

    public void clear() throws DataAccessException;

}