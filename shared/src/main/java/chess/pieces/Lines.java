package chess.pieces;
import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class Lines {
    private final int[][] directions;

    public Lines(int[][] directions) {
        this.directions = directions;
    }

    ArrayList<ChessMove> getPositions(ChessPosition start, ChessBoard board) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        for (int[] direction : directions) {
            int rowStep = direction[0];
            int colStep = direction[1];

            ChessPosition currentPosition =
                    new ChessPosition(start.getRow(), start.getColumn());

            while (true) {
                currentPosition = new ChessPosition(
                        currentPosition.getRow() + rowStep,
                        currentPosition.getColumn() + colStep
                );

                if (!currentPosition.insideBoard()) {
                    break;
                }

                var pieceOnSquare = board.getPiece(currentPosition);
                if (pieceOnSquare != null && pieceOnSquare.getTeamColor() == board.getPiece(start).getTeamColor()) {
                    break;
                }
                moves.add(new ChessMove(start, currentPosition, null));
                if (pieceOnSquare != null) {
                    break;
                }
            }
        }

        return moves;
    }
}
