package dataaccess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        createUserTable();
    }

    // Public Methods

    public void createUser(UserData userData) throws DataAccessException {

        validateUserData(userData);

        String hashedPassword =
                BCrypt.hashpw(userData.password(), BCrypt.gensalt());

        String sql = """
               INSERT INTO user (username, password, email)
               VALUES (?, ?, ?)
               """;

        executeUpdate(sql,
                userData.username(),
                hashedPassword,
                userData.email());
    }

    public UserData getUser(String username) throws DataAccessException {

        if (isBlank(username)) {
            return null;
        }

        String sql = """
               SELECT username, password, email
               FROM user
               WHERE username = ?
               """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet results = statement.executeQuery()) {

                if (results.next()) {

                    return new UserData(
                            results.getString("username"),
                            results.getString("password"),
                            results.getString("email")
                    );
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to read user", e);
        }

        return null;
    }

    public void clear() throws DataAccessException {
        executeUpdate("DELETE FROM user");
    }

    private void createUserTable() throws DataAccessException {

        String sql = """
               CREATE TABLE IF NOT EXISTS user (
                   username VARCHAR(255) NOT NULL,
                   password VARCHAR(255) NOT NULL,
                   email VARCHAR(255) NOT NULL,
                   PRIMARY KEY (username)
               )
               """;

        executeUpdate(sql);
    }

    // Helper Methods

    private void executeUpdate(String sql, Object... parameters) throws DataAccessException {

        try (var connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < parameters.length; i++) {
                setParameter(statement, i + 1, parameters[i]);
            }

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Database update failed", e);
        }
    }

    private void setParameter(PreparedStatement statement, int index, Object value)
            throws SQLException {

        if (value == null) {
            statement.setNull(index, Types.NULL);
        } else {
            statement.setObject(index, value);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validateUserData(UserData userData) throws DataAccessException {

        if (userData == null ||
                isBlank(userData.username()) ||
                isBlank(userData.password()) ||
                isBlank(userData.email())) {

            throw new DataAccessException("Invalid user data");
        }
    }
}
