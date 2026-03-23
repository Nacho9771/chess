package client;

import model.AuthData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;

import java.util.List;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        facade = new ServerFacade(port);
        System.out.println("Started test HTTP server on " + port);
    }

    @BeforeEach
    void clearDatabase() throws Exception {
        facade.clear();
    }

    @AfterAll
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

    @Test
    void listGamesPositive() throws Exception {
        AuthData auth = facade.register("player1", "password", "p1@email.com");
        facade.createGame(auth.authToken(), "first-game");
        facade.createGame(auth.authToken(), "second-game");

        List<GameSummary> games = facade.listGames(auth.authToken());

        Assertions.assertEquals(2, games.size());
        Assertions.assertTrue(games.stream().anyMatch(game -> "first-game".equals(game.gameName())));
        Assertions.assertTrue(games.stream().anyMatch(game -> "second-game".equals(game.gameName())));
    }

    @Test
    void listGamesNegativeUnauthorized() {
        ServerFacadeException ex = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.listGames("bad-token"));
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void joinGamePositive() throws Exception {
        AuthData creator = facade.register("creator", "password", "creator@email.com");
        int gameId = facade.createGame(creator.authToken(), "joinable");
        AuthData player = facade.register("player1", "password", "p1@email.com");

        facade.joinGame(player.authToken(), "WHITE", gameId);

        List<GameSummary> games = facade.listGames(player.authToken());
        GameSummary joinedGame = games.stream()
                .filter(game -> game.gameID() == gameId)
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals("player1", joinedGame.whiteUsername());
    }

    @Test
    void joinGameNegativeColorTaken() throws Exception {
        AuthData creator = facade.register("creator", "password", "creator@email.com");
        int gameId = facade.createGame(creator.authToken(), "joinable");
        AuthData whitePlayer = facade.register("whitePlayer", "password", "white@email.com");
        facade.joinGame(whitePlayer.authToken(), "WHITE", gameId);
        AuthData secondPlayer = facade.register("secondPlayer", "password", "second@email.com");

        ServerFacadeException ex = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.joinGame(secondPlayer.authToken(), "WHITE", gameId));
        Assertions.assertEquals("Error: already taken", ex.getMessage());
    }
}

