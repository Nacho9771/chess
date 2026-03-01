package dataaccess;
import model.GameData;
import java.util.ArrayList;

public class MemoryGameDAO implements GameDAO {

    private ArrayList<GameData> games = new ArrayList<>();

    public ArrayList<GameData> listGames() throws DataAccessException {
        return games;
    }

    public void clear() {
        games = new ArrayList<>();
    }

}