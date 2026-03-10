package dataaccess;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        configureDatabase();
    }

    public void createUser(UserData userData) throws DataAccessException {
        if (userData == null || userData.username() == null || userData.password() == null || userData.email() == null) {
            throw new DataAccessException("Invalid user");
        }

        var statement = "INSERT INTO `user` (username, password, email) VALUES (?, ?, ?)";
        var hashedPassword = BCrypt.hashpw(userData.password(), BCrypt.gensalt());

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, userData.username());
            ps.setString(2, hashedPassword);
            ps.setString(3, userData.email());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new DataAccessException("User already exists", ex);
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to create user", ex);
        }
    }

    public UserData getUser(String username) throws DataAccessException {
        var statement = "SELECT username, password, email FROM `user` WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to read user", ex);
        }

        return null;
    }

    public void clear() throws DataAccessException {
        var statement = "TRUNCATE TABLE `user`";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to clear users", ex);
        }
    }

    private void configureDatabase() throws DataAccessException {
        var createUserTable = """
                CREATE TABLE IF NOT EXISTS `user` (
                  username VARCHAR(256) NOT NULL,
                  password VARCHAR(100) NOT NULL,
                  email VARCHAR(256) NOT NULL,
                  PRIMARY KEY (username)
                )
                """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(createUserTable)) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to configure user table", ex);
        }
    }
}
