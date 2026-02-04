package chess.pieces;
import java.util.ArrayList;
import chess.*;

public class King implements AbstractPiece {

    @Override
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        int[][] adjacent = {{-1,-1},{-1,0},{-1,1},{0,1},{1,1},{1,0},{1,-1}, {0,-1}};

        for (int[] i : adjacent) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + i[0], myPosition.getColumn() + i[1]);

            if (!newPosition.insideBoard()) {continue;}

            if (board.getPiece(newPosition) != null && board.getPiece(myPosition).getTeamColor() == board.getPiece(newPosition).getTeamColor()) {
                continue;
            }

            possibleMoves.add(new ChessMove(myPosition, newPosition, null));
        }

        return possibleMoves;
    }
}