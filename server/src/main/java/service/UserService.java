package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import service.user.LoginRequest;
import service.user.RegisterRequest;

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
            throw error(403, "Error: already taken");
        }

        userDAO.createUser(new UserData(req.username(), req.password(), req.email()));
        return createAuthForUser(req.username());
    }

    public AuthResult login(LoginRequest req) throws ServiceException, DataAccessException {
        validateLogin(req);
        UserData user = userDAO.getUser(req.username());

        if (user == null || !user.password().equals(req.password())) {
            throw error(401, "Error: unauthorized");
        }

        return createAuthForUser(req.username());
    }

    public void logout(String authToken) throws ServiceException, DataAccessException {
        AuthData auth = requireAuth(authToken);
        authDAO.deleteAuth(auth.authToken());
    }

    private void validateRegister(RegisterRequest r) throws ServiceException {
        if (r == null || isBlank(r.username()) || isBlank(r.password()) || isBlank(r.email())) {
            throw error(400, "Error: bad request");
        }
    }

    private void validateLogin(LoginRequest r) throws ServiceException {
        if (r == null || isBlank(r.username()) || isBlank(r.password())) {
            throw error(400, "Error: bad request");
        }
    }

    private AuthResult createAuthForUser(String username) throws DataAccessException {
        String token = TokenUtil.generateToken();
        authDAO.createAuth(new AuthData(token, username));
        return new AuthResult(username, token);
    }

    private AuthData requireAuth(String token) throws ServiceException, DataAccessException {
        if (isBlank(token)) {
            throw error(401, "Error: unauthorized");
        }

        AuthData auth = authDAO.getAuth(token);
        if (auth == null) {
            throw error(401, "Error: unauthorized");
        }

        return auth;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private ServiceException error(int code, String msg) {
        return new ServiceException(code, msg);
    }
}
