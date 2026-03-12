package dataaccess;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

abstract class BaseSQLDAO {

    protected void executeUpdate(String sql, Object... parameters) throws DataAccessException {

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

    protected void setParameter(PreparedStatement statement, int index, Object value)
            throws SQLException {

        if (value == null) {
            statement.setNull(index, Types.NULL);
        } else {
            statement.setObject(index, value);
        }
    }

    protected boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
