package ui;

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

    }

    private boolean handlePostloginCommand(String input) {

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
        try {
            serverFacade.logout(authToken);
            authToken = null;
            username = null;
            lastListedGames = new ArrayList<>();
            System.out.println("Logged out.");
            printPreloginHelp();
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

    }

    private void playGame() {

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

    }

    private Integer parsePositiveNumber(String value) {

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
}

