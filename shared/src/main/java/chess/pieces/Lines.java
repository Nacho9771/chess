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

    ArrayList<ChessMove> getPositions(ChessPosition myPosition, ChessBoard board) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        for (int[] direction : directions) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn());
            boolean capturing = false;
            do {
                newPosition = new ChessPosition(newPosition.getRow() + direction[0], newPosition.getColumn() + direction[1]);

                if (!newPosition.insideBoard()) {
                    break;
                }

                if (board.getPiece(newPosition) != null) {
                    if (board.getPiece(myPosition).getTeamColor() == board.getPiece(newPosition).getTeamColor()) {
                        break;
                    } else {
                        capturing = true;
                    }
                }

                possibleMoves.add(new ChessMove(myPosition, newPosition, null));
            } while (!capturing);
        }

        return possibleMoves;
    }


}
