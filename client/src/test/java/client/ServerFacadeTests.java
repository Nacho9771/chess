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
        ServerFacade badFacade = new ServerFacade("http://localhost:67676767");

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
}