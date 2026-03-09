package dataaccess;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.Gson;
import model.GameData;

public class SQLGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    public SQLGameDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        configureDatabase();
    }

    public int createGame(GameData gameData) throws DataAccessException {
        throw new DataAccessException("Error: game ID was not generated");
    }

    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    public void updateGame(GameData newGameData) throws DataAccessException {

    }

    public Collection<GameData> listGames() throws DataAccessException {
        var games = new ArrayList<GameData>();

        return games;
    }

    public void clear() throws DataAccessException {
    }

    private void configureDatabase() throws DataAccessException {
    }
}
