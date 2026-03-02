package service.user;

import dataaccess.*;
import model.*;
import service.*;

public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthResult register(RegisterRequest req) throws ServiceException, DataAccessException {
        validateRegister(req);
        if (userDAO.getUser(req.username()) != null) {
            throw error(403, "Username Already Taken");
        }

        userDAO.createUser(new UserData(req.username(), req.password(), req.email()));
        return createAuthForUser(req.username());
    }

    private void validateRegister(RegisterRequest r) throws ServiceException {
        if (r == null || isBlank(r.username()) || isBlank(r.password()) || isBlank(r.email())) {
            throw error(400, "400: Bad Request");
        }
    }

    private AuthResult createAuthForUser(String username) throws DataAccessException {
        String token = TokenUtil.generateToken();
        authDAO.createAuth(new AuthData(token, username));
        return new AuthResult(username, token);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private ServiceException error(int code, String msg) {
        return new ServiceException(code, msg);
    }
}
