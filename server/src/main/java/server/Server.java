package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import io.javalin.http.Context;
import io.javalin.*;
import service.*;
import service.user.RegisterRequest;
import service.user.UserService;
import java.util.Map;


public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();
    private final UserService userService;
    private final ClearService clearService;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        userService = new UserService(userDAO, authDAO);
        clearService = new ClearService(userDAO, authDAO, gameDAO);

        javalin.delete("/db", this::handleClear);
        javalin.post("/user", this::handleRegister);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void handleClear(Context ctx) {
        try {
            clearService.clear();
            writeJson(ctx, 200, Map.of());
        } catch (DataAccessException ex) {
            internalError(ctx, ex);
        }
    }

    private void handleRegister(Context ctx) {
        try {
            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
            AuthResult result = userService.register(request);
            writeJson(ctx, 200, result);
        } catch (JsonSyntaxException | NullPointerException ex) {
            writeJson(ctx, 400, new ErrorResult("Error: bad request"));
        } catch (ServiceException ex) {
            writeJson(ctx, ex.statusCode(), new ErrorResult(ex.getMessage()));
        } catch (DataAccessException ex) {
            internalError(ctx, ex);
        }
    }

    private void writeJson(Context ctx, int statusCode, Object body) {
        ctx.status(statusCode);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(body));
    }

    private void internalError(Context ctx, Exception ex) {
        writeJson(ctx, 500, new ErrorResult("Error: " + ex.getMessage()));
    }


}