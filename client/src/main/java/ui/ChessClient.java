package ui;
//making regrade possible

import chess.ChessGame;
import client.GameSummary;
import client.ServerFacade;
import client.ServerFacadeException;
import model.AuthData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ChessClient {

    private final ServerFacade serverFacade;
    private final Scanner scanner = new Scanner(System.in);
    private final BoardPrinter boardPrinter = new BoardPrinter();
    private String authToken;
    private String username;
    private List<GameSummary> lastListedGames = new ArrayList<>();

    public ChessClient(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
    }

    public void run() {
        boolean running = true;
        printWelcome();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownCleanup));

        while (running && scanner.hasNextLine()) {
            try {
                printPrompt();
                String input = scanner.nextLine().trim();
                running = authToken == null ? handlePreloginCommand(input) : handlePostloginCommand(input);
            } catch (Exception ex) {
                System.out.println("Error: unable to process command");
            }
        }
    }

    private boolean handlePreloginCommand(String input) {
        String command = normalizeCommand(input);

        return switch (command) {
            case "help" -> {
                printPreloginHelp();
                yield true;
            }
            case "quit" -> false;
            case "login" -> {
                login();
                yield true;
            }
            case "register" -> {
                register();
                yield true;
            }
            case "" -> true;
            default -> {
                System.out.println("Error: unknown command. Type 'help' to see options.");
                yield true;
            }
        };
    }

    private boolean handlePostloginCommand(String input) {
        String command = normalizeCommand(input);

        return switch (command) {
            case "help" -> {
                printPostloginHelp();
                yield true;
            }
            case "logout" -> {
                logout();
                yield true;
            }
            case "create", "create game" -> {
                createGame();
                yield true;
            }
            case "list", "list games" -> {
                listGames();
                yield true;
            }
            case "play", "play game" -> {
                playGame();
                yield true;
            }
            case "observe", "observe game" -> {
                observeGame();
                yield true;
            }
            case "quit" -> {
                logout(false);
                yield false;
            }
            case "" -> true;
            default -> {
                System.out.println("Error: unknown command. Type 'help' to see options.");
                yield true;
            }
        };
    }

    private void register() {
        String enteredUsername = prompt("Username");
        String password = prompt("Password");
        String email = prompt("Email");

        try {
            AuthData authData = serverFacade.register(enteredUsername, password, email);
            setSession(authData);
            System.out.printf("Registered and logged in as %s.%n", username);
            printPostloginHelp();
        } catch (ServerFacadeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void login() {
        String enteredUsername = prompt("Username");
        String password = prompt("Password");

        try {
            AuthData authData = serverFacade.login(enteredUsername, password);
            setSession(authData);
            System.out.printf("Logged in as %s.%n", username);
            printPostloginHelp();
        } catch (ServerFacadeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void logout() {
        logout(true);
    }

    private void logout(boolean showHelp) {
        try {
            if (authToken == null) {
                return;
            }
            serverFacade.logout(authToken);
            authToken = null;
            username = null;
            lastListedGames = new ArrayList<>();
            System.out.println("Logged out.");
            if (showHelp) {
                printPreloginHelp();
            }
        } catch (ServerFacadeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void createGame() {
        String gameName = prompt("Game name");

        try {
            serverFacade.createGame(authToken, gameName);
            System.out.printf("Created game '%s'.%n", gameName);
        } catch (ServerFacadeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void listGames() {
        try {
            lastListedGames = serverFacade.listGames(authToken);
            if (lastListedGames.isEmpty()) {
                System.out.println("No games found.");
                return;
            }

            for (int index = 0; index < lastListedGames.size(); index++) {
                GameSummary game = lastListedGames.get(index);
                System.out.printf("%d. %s | White: %s | Black: %s%n",
                        index + 1,
                        game.gameName(),
                        displayPlayer(game.whiteUsername()),
                        displayPlayer(game.blackUsername()));
            }
        } catch (ServerFacadeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void playGame() {
        GameSummary selectedGame = promptForGameSelection();
        if (selectedGame == null) {
            return;
        }

        ChessGame.TeamColor color = promptForColor();
        if (color == null) {
            return;
        }

        try {
            serverFacade.joinGame(authToken, color.name(), selectedGame.gameID());
            System.out.printf("Joined '%s' as %s.%n", selectedGame.gameName(), color.name().toLowerCase(Locale.ROOT));
            boardPrinter.drawBoard(color);
        } catch (ServerFacadeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void observeGame() {
        GameSummary selectedGame = promptForGameSelection();
        if (selectedGame == null) {
            return;
        }

        System.out.printf("Observing '%s'.%n", selectedGame.gameName());
        boardPrinter.drawBoard(ChessGame.TeamColor.WHITE);
    }

    private GameSummary promptForGameSelection() {
        if (lastListedGames.isEmpty()) {
            System.out.println("List games first so you can choose one by number.");
            return null;
        }

        String numberText = prompt("Game number");
        Integer gameNumber = parsePositiveNumber(numberText);
        if (gameNumber == null || gameNumber < 1 || gameNumber > lastListedGames.size()) {
            System.out.println("Error: invalid game number");
            return null;
        }

        return lastListedGames.get(gameNumber - 1);
    }

    private ChessGame.TeamColor promptForColor() {
        String colorText = prompt("Color (white or black)");
        if (colorText.equalsIgnoreCase("white")) {
            return ChessGame.TeamColor.WHITE;
        }
        if (colorText.equalsIgnoreCase("black")) {
            return ChessGame.TeamColor.BLACK;
        }

        System.out.println("Error: color must be white or black");
        return null;
    }

    private Integer parsePositiveNumber(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String displayPlayer(String player) {
        return player == null || player.isBlank() ? "(open)" : player;
    }

    private void setSession(AuthData authData) {
        authToken = authData.authToken();
        username = authData.username();
        lastListedGames = new ArrayList<>();
    }

    private void printWelcome() {
        System.out.println("240 Chess Client");
        printPreloginHelp();
    }

    private void printPrompt() {
        String state = authToken == null ? "prelogin" : username;
        System.out.printf("[%s] >>> ", state);
    }

    private String prompt(String label) {
        System.out.print(label + ": ");
        if (!scanner.hasNextLine()) {
            return "";
        }
        return scanner.nextLine().trim();
    }

    private String normalizeCommand(String input) {
        return input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
    }

    private void printPreloginHelp() {
        System.out.println("help - show available commands");
        System.out.println("register - create a new account");
        System.out.println("login - sign in to an existing account");
        System.out.println("quit - exit the program");
    }

    private void printPostloginHelp() {
        System.out.println("help - show available commands");
        System.out.println("create game - create a new game");
        System.out.println("list games - list current games");
        System.out.println("play game - join a listed game as white or black");
        System.out.println("observe game - view a listed game");
        System.out.println("logout - sign out");
        System.out.println("quit - exit the program");
    }

    private void shutdownCleanup() {
        try {
            if (authToken != null) {
                serverFacade.logout(authToken);
            }
        } catch (ServerFacadeException ignored) {
        }
    }
}

