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
import org.junit.jupiter.api.AfterAll;
import service.*;
import service.create.CreateGameRequest;
import service.create.CreateGameResult;
import service.join.JoinGameRequest;
import service.list.ListGamesResult;
import service.user.LoginRequest;
import service.user.RegisterRequest;
import service.user.UserService;
import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(authDAO, gameDAO);
        clearService = new ClearService(userDAO, authDAO, gameDAO);

        javalin.delete("/db", this::handleClear);
        javalin.post("/user", this::handleRegister);
        javalin.post("/session", this::handleLogin);
        javalin.delete("/session", this::handleLogout);
        javalin.get("/game", this::handleListGames);
        javalin.post("/game", this::handleCreateGame);
        javalin.put("/game", this::handleJoinGame);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    @AfterAll
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

    private void handleLogin(Context ctx) {
        try {
            LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
            AuthResult result = userService.login(request);
            writeJson(ctx, 200, result);
        } catch (JsonSyntaxException | NullPointerException ex) {
            writeJson(ctx, 400, new ErrorResult("Error: bad request"));
        } catch (ServiceException ex) {
            writeJson(ctx, ex.statusCode(), new ErrorResult(ex.getMessage()));
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

    private void handleCreateGame(Context ctx) {
        try {
            CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
            CreateGameResult result = gameService.createGame(ctx.header("authorization"), request);
            writeJson(ctx, 200, result);
        } catch (JsonSyntaxException | NullPointerException ex) {
            writeJson(ctx, 400, new ErrorResult("Error: bad request"));
        } catch (ServiceException ex) {
            writeJson(ctx, ex.statusCode(), new ErrorResult(ex.getMessage()));
        } catch (DataAccessException ex) {
            internalError(ctx, ex);
        }
    }

    private void handleJoinGame(Context ctx) {
        try {
            JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
            gameService.joinGame(ctx.header("authorization"), request);
            writeJson(ctx, 200, Map.of());
        } catch (JsonSyntaxException | NullPointerException ex) {
            writeJson(ctx, 400, new ErrorResult("Error: bad request"));
        } catch (ServiceException ex) {
            writeJson(ctx, ex.statusCode(), new ErrorResult(ex.getMessage()));
        } catch (DataAccessException ex) {
            internalError(ctx, ex);
        }
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
