package chess.pieces;
import java.util.ArrayList;
import chess.*;

public class Bishop implements AbstractPiece {

    @Override
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] directions = {{1,1},{-1,1},{-1,-1},{1,-1}};
        Lines newLineMoves = new Lines(directions);
        return newLineMoves.getPositions(myPosition, board);
    }
}