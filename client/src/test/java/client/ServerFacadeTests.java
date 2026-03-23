package client;

import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import server.Server;

import java.util.List;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    public static void init() {
        server = new Server();
        int port = server.run(0);
        facade = new ServerFacade(port);
        System.out.println("Started test HTTP server on " + port);
    }

    void clearDatabase() throws Exception {
        facade.clear();
    }

    static void stopServer() {
        server.stop();
    }

    @Test
    void clearPositive() {
        Assertions.assertDoesNotThrow(() -> facade.clear());
    }

    @Test
    void clearNegativeBadServer() {
        ServerFacade badFacade = new ServerFacade("http://localhost:1");

        ServerFacadeException ex = Assertions.assertThrows(ServerFacadeException.class, badFacade::clear);
        Assertions.assertEquals("Error: unable to reach server", ex.getMessage());
    }

    @Test
    void clearPositiveRemovesData() throws Exception {
        AuthData auth = facade.register("clear-user", "password", "clear@email.com");
        facade.createGame(auth.authToken(), "before-clear");

        facade.clear();

        AuthData secondAuth = facade.register("clear-user", "password", "clear@email.com");
        List<GameSummary> games = facade.listGames(secondAuth.authToken());
        Assertions.assertTrue(games.isEmpty());
    }

    @Test
    void registerPositive() throws Exception {
        AuthData auth = facade.register("player1", "password", "p1@email.com");

        Assertions.assertEquals("player1", auth.username());
        Assertions.assertNotNull(auth.authToken());
        Assertions.assertTrue(auth.authToken().length() > 10);
    }

    @Test
    void registerNegativeDuplicateUser() throws Exception {
        facade.register("player1", "password", "p1@email.com");

        ServerFacadeException ex = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.register("player1", "password", "p1@email.com"));
        Assertions.assertEquals("Error: already taken", ex.getMessage());
    }

    @Test
    void loginPositive() throws Exception {
        facade.register("player1", "password", "p1@email.com");

        AuthData auth = facade.login("player1", "password");

        Assertions.assertEquals("player1", auth.username());
        Assertions.assertNotNull(auth.authToken());
    }

    @Test
    void loginNegativeWrongPassword() throws Exception {
        facade.register("player1", "password", "p1@email.com");

        ServerFacadeException ex = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.login("player1", "wrong-password"));
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void logoutPositive() throws Exception {
        AuthData auth = facade.register("player1", "password", "p1@email.com");

        facade.logout(auth.authToken());

        ServerFacadeException ex = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.listGames(auth.authToken()));
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void logoutNegativeBadToken() {
        ServerFacadeException ex = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.logout("bad-token"));
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void createGamePositive() throws Exception {
        AuthData auth = facade.register("creator", "password", "creator@email.com");

        int gameId = facade.createGame(auth.authToken(), "new-game");

        Assertions.assertTrue(gameId > 0);
    }

    @Test
    void createGameNegativeNoAuth() {
        ServerFacadeException ex = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.createGame("bad-token", "new-game"));
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }
}
