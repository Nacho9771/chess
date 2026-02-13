package chess.pieces;
import java.util.ArrayList;
import chess.*;

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

            ChessPosition myPosition = new ChessPosition(start.getRow(), start.getColumn());

            while (true) {
                myPosition = new ChessPosition(myPosition.getRow() + rowStep, myPosition.getColumn() + colStep);

                // Checks for boundaries
                if (!myPosition.insideBoard()) {
                    break;
                }

                // Checks for available square/capture
                var pieceOnSquare = board.getPiece(myPosition);
                if (pieceOnSquare != null && pieceOnSquare.getTeamColor() == board.getPiece(start).getTeamColor()) {
                    break;
                }
                moves.add(new ChessMove(start, myPosition, null));
                if (pieceOnSquare != null) {
                    break;
                }
            }
        }

        return moves;
    }
}
