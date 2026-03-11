package dataaccess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.AuthData;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        createAuthTable();
    }

    // Public Methods

    public void createAuth(AuthData authData) throws DataAccessException {

        validateAuthData(authData);

        String sql = """
                INSERT INTO auth (authToken, username)
                VALUES (?, ?)
                """;

        executeUpdate(sql, authData.authToken(), authData.username());
    }

    public AuthData getAuth(String authToken) throws DataAccessException {

        if (isBlank(authToken)) {
            return null;
        }

        String sql = """
                SELECT authToken, username
                FROM auth
                WHERE authToken = ?
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, authToken);

            try (ResultSet results = statement.executeQuery()) {

                if (results.next()) {
                    String token = results.getString("authToken");
                    String username = results.getString("username");

                    return new AuthData(token, username);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to read auth from database", e);
        }

        return null;
    }

    public void deleteAuth(String authToken) throws DataAccessException {

        if (isBlank(authToken)) {
            throw new DataAccessException("Invalid auth token");
        }

        String sql = "DELETE FROM auth WHERE authToken = ?";
        executeUpdate(sql, authToken);
    }

    public void clear() throws DataAccessException {
        executeUpdate("DELETE FROM auth");
    }

    /**
     * Creates the table if it hasn't been created
     */
    private void createAuthTable() throws DataAccessException {

        String sql = """
                CREATE TABLE IF NOT EXISTS auth (
                    authToken VARCHAR(255) NOT NULL,
                    username VARCHAR(255) NOT NULL,
                    PRIMARY KEY (authToken)
                )
                """;

        executeUpdate(sql);
    }

    // Helper Methods

    /**
     * Runs an update query (INSERT, DELETE, UPDATE)
     */
    private void executeUpdate(String sql, Object... parameters) throws DataAccessException {

    }

    private void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {

    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validateAuthData(AuthData authData) throws DataAccessException {

        if (authData == null ||
                isBlank(authData.authToken()) ||
                isBlank(authData.username())) {

            throw new DataAccessException("Invalid auth data");
        }
    }
}