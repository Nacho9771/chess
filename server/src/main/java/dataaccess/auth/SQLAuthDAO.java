package dataaccess;

import java.sql.SQLException;

import model.AuthData;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        configureDatabase();
    }

    public void createAuth(AuthData authData) throws DataAccessException {
        if (authData == null || authData.authToken() == null || authData.username() == null) {
            throw new DataAccessException("Invalid auth");
        }

        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, authData.authToken());
            ps.setString(2, authData.username());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to create auth", ex);
        }
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        var statement = "SELECT authToken, username FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, authToken);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(rs.getString("authToken"), rs.getString("username"));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to read auth", ex);
        }

        return null;
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, authToken);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to delete auth", ex);
        }
    }

    public void clear() throws DataAccessException {
        var statement = "TRUNCATE TABLE auth";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to clear auth", ex);
        }
    }

    private void configureDatabase() throws DataAccessException {
        var createAuthTable = """
                CREATE TABLE IF NOT EXISTS auth (
                  authToken VARCHAR(256) NOT NULL,
                  username VARCHAR(256) NOT NULL,
                  PRIMARY KEY (authToken),
                  INDEX (username)
                )
                """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(createAuthTable)) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to configure auth table", ex);
        }
    }
}


