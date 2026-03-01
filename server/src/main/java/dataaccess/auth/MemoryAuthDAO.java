package dataaccess;
import model.AuthData;
import java.util.ArrayList;

public class MemoryAuthDAO implements AuthDAO {

    private ArrayList<AuthData> auths = new ArrayList<>();

    public void createAuth(AuthData authData) throws DataAccessException {
        auths.add(authData);
    }

    public void clear() {
        auths = new ArrayList<>();
    }

}