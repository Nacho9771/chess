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
            throw ServiceUtil.alreadyTaken();
        }

        userDAO.createUser(new UserData(req.username(), req.password(), req.email()));
        return createAuthForUser(req.username());
    }

    public AuthResult login(LoginRequest req) throws ServiceException, DataAccessException {

        validateLogin(req);
        UserData user = userDAO.getUser(req.username());

        if (user == null || !user.password().equals(req.password())) {
            throw ServiceUtil.unauthorized();
        }

        return createAuthForUser(req.username());
    }

    public void logout(String authToken) throws ServiceException, DataAccessException {

        // Remove the auth token to invalidate the session.
        AuthData auth = ServiceUtil.requireAuth(authToken, authDAO);
        authDAO.deleteAuth(auth.authToken());
    }

    private void validateRegister(RegisterRequest r) throws ServiceException {

        if (r == null || ServiceUtil.isBlank(r.username())
                || ServiceUtil.isBlank(r.password()) || ServiceUtil.isBlank(r.email())) {
            throw ServiceUtil.badRequest();
        }
    }

    private void validateLogin(LoginRequest r) throws ServiceException {

        if (r == null || ServiceUtil.isBlank(r.username()) || ServiceUtil.isBlank(r.password())) {
            throw ServiceUtil.badRequest();
        }
    }

    private AuthResult createAuthForUser(String username) throws DataAccessException {

        // Create a fresh auth token on each login or registration.
        String token = TokenUtil.generateToken();
        authDAO.createAuth(new AuthData(token, username));

        return new AuthResult(username, token);
    }
}
