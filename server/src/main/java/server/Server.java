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
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.ClearService;
import service.ErrorResult;
import service.GameService;
import service.ServiceException;
import service.UserService;
import service.create.CreateGameRequest;
import service.join.JoinGameRequest;
import service.list.ListGamesResult;
import service.user.LoginRequest;
import service.user.RegisterRequest;
import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;

    public Server() {
        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
        });

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(authDAO, gameDAO);
        clearService = new ClearService(userDAO, authDAO, gameDAO);
        registerRoutes();
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    // ------------------------ Handlers ------------------------

    private void registerRoutes() {
        // Route handlers delegate to services and return JSON results.
        javalin.delete("/db", this::handleClear);
        javalin.post("/user", ctx -> handleJsonRequest(ctx, RegisterRequest.class, userService::register));
        javalin.post("/session", ctx -> handleJsonRequest(ctx, LoginRequest.class, userService::login));
        javalin.delete("/session", this::handleLogout);
        javalin.get("/game", this::handleListGames);
        javalin.post("/game", ctx -> handleJsonRequest(
                ctx,
                CreateGameRequest.class,
                req -> gameService.createGame(ctx.header("authorization"), req)
        ));
        javalin.put("/game", ctx -> handleJsonRequest(ctx, JoinGameRequest.class, req -> {
            gameService.joinGame(ctx.header("authorization"), req);
            return Map.of();
        }));
    }

    private void handleClear(Context ctx) {
        try {
            clearService.clear();
            writeJson(ctx, 200, Map.of());
        } catch (DataAccessException ex) {
            internalError(ctx, ex);
        }
    }

    private void handleLogout(Context ctx) {
        try {
            userService.logout(ctx.header("authorization"));
            writeJson(ctx, 200, Map.of());
        } catch (ServiceException ex) {
            writeJson(ctx, ex.statusCode(), new ErrorResult(ex.getMessage()));
        } catch (DataAccessException ex) {
            internalError(ctx, ex);
        }
    }

    private void handleListGames(Context ctx) {
        try {
            ListGamesResult result = gameService.listGames(ctx.header("authorization"));
            writeJson(ctx, 200, result);
        } catch (ServiceException ex) {
            writeJson(ctx, ex.statusCode(), new ErrorResult(ex.getMessage()));
        } catch (DataAccessException ex) {
            internalError(ctx, ex);
        }
    }

    /**
     * Generic JSON request handler to reduce repetition.
     */
    private <T, R> void handleJsonRequest(Context ctx, Class<T> requestClass, ServiceHandler<T, R> serviceMethod) {
        try {
            T request = gson.fromJson(ctx.body(), requestClass);
            R result = serviceMethod.apply(request);
            writeJson(ctx, 200, result);
        } catch (JsonSyntaxException | NullPointerException ex) {
            writeJson(ctx, 400, new ErrorResult("Error: bad request"));
        } catch (ServiceException ex) {
            writeJson(ctx, ex.statusCode(), new ErrorResult(ex.getMessage()));
        } catch (DataAccessException ex) {
            internalError(ctx, ex);
        }
    }

    @FunctionalInterface
    interface ServiceHandler<T, R> {
        R apply(T t) throws ServiceException, DataAccessException;
    }

    private void internalError(Context ctx, Exception ex) {
        writeJson(ctx, 500, new ErrorResult("Error: " + ex.getMessage()));
    }

    private void writeJson(Context ctx, int statusCode, Object body) {
        ctx.status(statusCode);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(body));
    }
}
