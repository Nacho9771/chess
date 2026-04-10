package ui;
//making regrade possible

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import client.GameSummary;
import client.GameWebSocketClient;
import client.ServerFacade;
import client.ServerFacadeException;
import model.AuthData;
import model.GameData;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.ArrayList;
import java.util.Collection;
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
    private GameSession gameSession;
    private GameWebSocketClient webSocketClient;

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
        if (gameSession != null) {
            return handleInGameCommand(input);
        }

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
            gameSession = null;
            closeWebSocket();
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

    private boolean handleInGameCommand(String input) {
        String command = normalizeCommand(input);

        return switch (command) {
            case "help" -> {
                printInGameHelp();
                yield true;
            }
            case "redraw", "redraw board" -> {
                redrawCurrentBoard();
                yield true;
            }
            case "leave" -> {
                leaveCurrentGame();
                yield true;
            }
            case "resign" -> {
                resignCurrentGame();
                yield true;
            }
            case "move", "make move" -> {
                makeMove();
                yield true;
            }
            case "highlight", "highlight legal moves" -> {
                highlightLegalMoves();
                yield true;
            }
            case "quit" -> {
                shutdownCleanup();
                yield false;
            }
            case "" -> true;
            default -> {
                System.out.println("Error: unknown command. Type 'help' to see options.");
                yield true;
            }
        };
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
            enterGame(selectedGame, color, false);
        } catch (ServerFacadeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void observeGame() {
        GameSummary selectedGame = promptForGameSelection();
        if (selectedGame == null) {
            return;
        }

        try {
            System.out.printf("Observing '%s'.%n", selectedGame.gameName());
            enterGame(selectedGame, ChessGame.TeamColor.WHITE, true);
        } catch (ServerFacadeException ex) {
            System.out.println(ex.getMessage());
        }
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
        String state;
        if (authToken == null) {
            state = "prelogin";
        } else if (gameSession != null) {
            state = gameSession.promptLabel();
        } else {
            state = username;
        }
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

    private void printInGameHelp() {
        if (gameSession == null) {
            printPostloginHelp();
            return;
        }

        System.out.println("help - show available commands");
        System.out.println("redraw board - display the current board");
        System.out.println("highlight legal moves - show valid moves for a piece");
        if (!gameSession.observer()) {
            System.out.println("make move - make a move on your turn");
            System.out.println("resign - resign the current game");
        }
        System.out.println("leave - leave the current game");
        System.out.println("quit - exit the program");
    }

    private void enterGame(GameSummary game, ChessGame.TeamColor perspective, boolean observer) throws ServerFacadeException {
        closeWebSocket();
        webSocketClient = new GameWebSocketClient(
                serverFacade.getWebSocketUrl(),
                this::handleLoadGame,
                this::handleNotification,
                this::handleErrorMessage,
                this::handleWebSocketError,
                this::handleWebSocketClosed
        );
        gameSession = new GameSession(game.gameID(), game.gameName(), perspective, observer, null);
        webSocketClient.connect(authToken, game.gameID());
        printInGameHelp();
    }

    private synchronized void handleLoadGame(LoadGameMessage message) {
        if (gameSession == null) {
            return;
        }
        gameSession = gameSession.withGame(message.getGame());
        redrawCurrentBoard();
        printPrompt();
    }

    private void handleWebSocketError(Throwable error) {
        System.out.println("Error: websocket connection failed");
        if (error != null && error.getMessage() != null && !error.getMessage().isBlank()) {
            System.out.println(error.getMessage());
        }
        if (authToken != null) {
            printPrompt();
        }
    }

    private synchronized void handleWebSocketClosed(String reason) {
        if (gameSession == null || authToken == null) {
            return;
        }

        try {
            GameSession session = gameSession;
            webSocketClient = new GameWebSocketClient(
                    serverFacade.getWebSocketUrl(),
                    this::handleLoadGame,
                    this::handleNotification,
                    this::handleErrorMessage,
                    this::handleWebSocketError,
                    this::handleWebSocketClosed
            );
            webSocketClient.connect(authToken, session.gameId());
        } catch (ServerFacadeException ex) {
            System.out.println("Error: websocket closed");
            if (reason != null && !reason.isBlank()) {
                System.out.println(reason);
            }
            System.out.println(ex.getMessage());
            printPrompt();
        }
    }

    private synchronized void handleNotification(NotificationMessage message) {
        System.out.println(message.getMessage());
        if (authToken != null) {
            printPrompt();
        }
    }

    private synchronized void handleErrorMessage(ErrorMessage message) {
        System.out.println(message.getErrorMessage());
        if (authToken != null) {
            printPrompt();
        }
    }

    private synchronized void redrawCurrentBoard() {
        if (gameSession == null) {
            return;
        }
        ChessGame game = gameSession.currentGame() == null ? null : gameSession.currentGame().game();
        boardPrinter.drawBoard(game, gameSession.perspective());
    }

    private void leaveCurrentGame() {
        if (gameSession == null) {
            return;
        }
        if (webSocketClient != null) {
            try {
                webSocketClient.leave(authToken, gameSession.gameId());
            } catch (ServerFacadeException ex) {
                System.out.println(ex.getMessage());
            }
        }
        gameSession = null;
        closeWebSocket();
        printPostloginHelp();
    }

    private void resignCurrentGame() {
        if (gameSession == null || gameSession.observer()) {
            System.out.println("Error: observers cannot resign");
            return;
        }
        if (!confirmResignation()) {
            System.out.println("Resignation cancelled.");
            return;
        }
        if (webSocketClient != null) {
            try {
                webSocketClient.resign(authToken, gameSession.gameId());
            } catch (ServerFacadeException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private void makeMove() {
        if (gameSession == null) {
            return;
        }
        if (gameSession.observer()) {
            System.out.println("Error: observers cannot make moves");
            return;
        }

        ChessPosition start = parsePosition(prompt("From"));
        ChessPosition end = parsePosition(prompt("To"));
        if (start == null || end == null) {
            System.out.println("Error: positions must be like e2 or h8");
            return;
        }

        ChessPiece.PieceType promotion = null;
        if (requiresPromotion(start, end)) {
            promotion = parsePromotion(prompt("Promotion piece (queen, rook, bishop, knight)"));
        }
        if (promotion == ChessPiece.PieceType.KING || promotion == ChessPiece.PieceType.PAWN) {
            System.out.println("Error: invalid promotion piece");
            return;
        }

        if (webSocketClient != null) {
            try {
                webSocketClient.makeMove(authToken, gameSession.gameId(), new ChessMove(start, end, promotion));
            } catch (ServerFacadeException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private boolean requiresPromotion(ChessPosition start, ChessPosition end) {
        if (gameSession == null || gameSession.currentGame() == null) {
            return false;
        }

        ChessPiece piece = gameSession.currentGame().game().getBoard().getPiece(start);
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return false;
        }

        int promotionRow = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 8 : 1;
        return end.getRow() == promotionRow;
    }

    private void highlightLegalMoves() {
        if (gameSession == null || gameSession.currentGame() == null) {
            System.out.println("Error: game state is not loaded yet");
            return;
        }

        ChessPosition position = parsePosition(prompt("Piece position"));
        if (position == null) {
            System.out.println("Error: position must be like e2 or h8");
            return;
        }

        Collection<ChessMove> moves = gameSession.currentGame().game().validMoves(position);
        if (moves == null || moves.isEmpty()) {
            System.out.println("No legal moves.");
            return;
        }
        boardPrinter.drawBoard(gameSession.currentGame().game(), gameSession.perspective(), moves);
    }

    private boolean confirmResignation() {
        while (true) {
            String confirmation = prompt("Are you sure you want to resign? y/n");
            if (confirmation.equalsIgnoreCase("y")) {
                return true;
            }
            if (confirmation.equalsIgnoreCase("n")) {
                return false;
            }
            System.out.println("Please enter y or n.");
        }
    }

    private ChessPosition parsePosition(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() != 2) {
            return null;
        }

        char file = normalized.charAt(0);
        char rank = normalized.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            return null;
        }

        return new ChessPosition(rank - '0', (file - 'a') + 1);
    }

    private ChessPiece.PieceType parsePromotion(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "queen" -> ChessPiece.PieceType.QUEEN;
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            default -> ChessPiece.PieceType.KING;
        };
    }

    private void closeWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
    }

    private void shutdownCleanup() {
        try {
            GameSession session = gameSession;
            gameSession = null;
            if (session != null && webSocketClient != null) {
                webSocketClient.leave(authToken, session.gameId());
            }
            closeWebSocket();
            if (authToken != null) {
                serverFacade.logout(authToken);
            }
        } catch (ServerFacadeException ignored) {
        }
    }

    private record GameSession(int gameId, String gameName, ChessGame.TeamColor perspective, boolean observer,
                               GameData currentGame) {
        private GameSession withGame(GameData gameData) {
            return new GameSession(gameId, gameName, perspective, observer, gameData);
        }

        private String promptLabel() {
            return observer ? gameName + ":observe" : gameName + ":" + perspective.name().toLowerCase(Locale.ROOT);
        }
    }

}

