package chess.pieces;

import java.util.ArrayList;
import chess.*;

public class Knight implements AbstractPiece {

    @Override
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        int[][] jumps = {
                {1,2},{1,-2},{-1,2},{-1,-2},
                {2,1},{2,-1},{-2,1},{-2,-1}
        };

        for (int[] j : jumps) {
            ChessPosition newPosition = new ChessPosition(
                    myPosition.getRow() + j[0],
                    myPosition.getColumn() + j[1]);

            if (!newPosition.insideBoard()) continue;

            if (board.getPiece(newPosition) != null &&
                    board.getPiece(myPosition).getTeamColor() ==
                            board.getPiece(newPosition).getTeamColor()) {
                continue;
            }

            possibleMoves.add(new ChessMove(myPosition, newPosition, null));
        }

        return possibleMoves;
    }
}
