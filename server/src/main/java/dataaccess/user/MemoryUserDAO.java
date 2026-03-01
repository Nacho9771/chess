package dataaccess;
import model.UserData;
import java.util.ArrayList;

public class MemoryUserDAO implements UserDAO {

    private ArrayList<UserData> users = new ArrayList<>();

    public void clear() {
        users = new ArrayList<>();
    }
}