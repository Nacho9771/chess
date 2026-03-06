package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

public final class ServiceUtil {

    private ServiceUtil() {}

    // Shared auth and validation helpers for service classes.
    public static AuthData requireAuth(String token, AuthDAO authDAO) throws ServiceException, DataAccessException {

        if (isBlank(token)) {
            throw unauthorized();
        }

        AuthData auth = authDAO.getAuth(token);
        if (auth == null) {
            throw unauthorized();
        }

        return auth;
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static ServiceException badRequest() {
        return new ServiceException(400, "Error: bad request");
    }

    public static ServiceException unauthorized() {
        return new ServiceException(401, "Error: unauthorized");
    }

    public static ServiceException alreadyTaken() {
        return new ServiceException(403, "Error: already taken");
    }
}
