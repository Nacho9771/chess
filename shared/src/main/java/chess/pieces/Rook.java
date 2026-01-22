package chess.pieces;

import java.util.ArrayList;
import chess.*;

public class Rook implements AbstractPiece {

    @Override
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] directions = {{1,0},{-1,0},{0,1},{0,-1}};
        Lines newLineMoves = new Lines(directions);
        return newLineMoves.getPositions(myPosition, board);
    }
}
