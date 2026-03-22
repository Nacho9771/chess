package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class BoardPrinter {

    private static final String LIGHT_SQUARE = EscapeSequences.SET_BG_COLOR_WHITE;
    private static final String DARK_SQUARE = EscapeSequences.SET_BG_COLOR_DARK_GREEN;
    private static final String BORDER_BACKGROUND = EscapeSequences.SET_BG_COLOR_BLACK;
    private static final String BORDER_TEXT = EscapeSequences.SET_TEXT_COLOR_WHITE;
    private static final String WHITE_PIECE_TEXT = EscapeSequences.SET_TEXT_COLOR_RED;
    private static final String BLACK_PIECE_TEXT = EscapeSequences.SET_TEXT_COLOR_BLUE;

    public void drawBoard(ChessGame.TeamColor view) {
        ChessGame game = new ChessGame();

        int startRow = (view == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int endRow = (view == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int startCol = (view == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int endCol = (view == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int rowStep = (startRow > endRow) ? -1 : 1;
        int colStep = (startCol > endCol) ? -1 : 1;

        System.out.print(EscapeSequences.ERASE_SCREEN);
        printFileLabels(range(startCol, endCol, colStep));

        for (int r = startRow; r != endRow + rowStep; r += rowStep) {
            printBoardRow(game, r, range(startCol, endCol, colStep));
        }

        printFileLabels(range(startCol, endCol, colStep));

        System.out.print(EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println();
    }

    private int[] range(int start, int end, int step) {
        int size = Math.abs(end - start) + 1;
        int[] arr = new int[size];
        int i = 0;

        for (int v = start; step > 0 ? v <= end : v >= end; v += step) {
            arr[i++] = v;
        }
        return arr;
    }

    private void printFileLabels(int[] columns) {
        System.out.print(BORDER_BACKGROUND + BORDER_TEXT + "    ");
        for (int column : columns) {
            System.out.print(" " + (char) ('a' + column - 1) + "  ");
        }
        System.out.print(EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println();
    }

    private void printBoardRow(ChessGame game, int row, int[] columns) {
        System.out.print(BORDER_BACKGROUND + BORDER_TEXT + " " + row + " ");
        for (int column : columns) {
            String squareColor = isLightSquare(row, column) ? LIGHT_SQUARE : DARK_SQUARE;
            System.out.print(squareColor);
            System.out.print(pieceDisplay(game, row, column));
        }
        System.out.print(BORDER_BACKGROUND + BORDER_TEXT + " " + row + " ");
        System.out.print(EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println();
    }

    private boolean isLightSquare(int row, int column) {
        return (row + column) % 2 == 1;
    }

    private String pieceDisplay(ChessGame game, int row, int column) {
        ChessPiece piece = game.getBoard().getPiece(new ChessPosition(row, column));
        if (piece == null) {
            return EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.EMPTY;
        }

        return piece.getTeamColor() == ChessGame.TeamColor.WHITE
                ? WHITE_PIECE_TEXT + pieceSymbol(piece)
                : BLACK_PIECE_TEXT + pieceSymbol(piece);
    }

    private String pieceSymbol(ChessPiece piece) {
        return getSymbol(piece.getPieceType(), piece.getTeamColor());
    }

    private String getSymbol(ChessPiece.PieceType type, ChessGame.TeamColor color) {
        return getColorSymbol(type, color);
    }

    private String colorSymbol(ChessGame.TeamColor color, String white, String black) {
        return color == ChessGame.TeamColor.WHITE ? white : black;
    }

    private String getColorSymbol(ChessPiece.PieceType type, ChessGame.TeamColor color) {
        return switch (type) {
            case KING -> colorSymbol(color, EscapeSequences.WHITE_KING, EscapeSequences.BLACK_KING);
            case QUEEN -> colorSymbol(color, EscapeSequences.WHITE_QUEEN, EscapeSequences.BLACK_QUEEN);
            case BISHOP -> colorSymbol(color, EscapeSequences.WHITE_BISHOP, EscapeSequences.BLACK_BISHOP);
            case KNIGHT -> colorSymbol(color, EscapeSequences.WHITE_KNIGHT, EscapeSequences.BLACK_KNIGHT);
            case ROOK -> colorSymbol(color, EscapeSequences.WHITE_ROOK, EscapeSequences.BLACK_ROOK);
            case PAWN -> colorSymbol(color, EscapeSequences.WHITE_PAWN, EscapeSequences.BLACK_PAWN);
        };
    }
}

