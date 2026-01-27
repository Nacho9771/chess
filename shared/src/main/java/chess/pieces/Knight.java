package chess.pieces;
import java.util.ArrayList;
import chess.*;

public class Knight implements AbstractPiece {

    @Override
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition start) {

        ArrayList<ChessMove> moves = new ArrayList<>();
        ChessPiece knight = board.getPiece(start);

        int[][] jumps = {
            { 1,  2}, { 1, -2}, {-1,  2}, {-1, -2},
            { 2,  1}, { 2, -1}, {-2,  1}, {-2, -1}
        };

        for (int[] jump : jumps) {
            ChessPosition end = new ChessPosition(start.getRow() + jump[0], start.getColumn() + jump[1]);
            if (!end.insideBoard()) {
                continue;
            }

            ChessPiece pieceOnSquare = board.getPiece(end);
            if (pieceOnSquare != null && pieceOnSquare.getTeamColor() == knight.getTeamColor()) {
                continue;
            }
            moves.add(new ChessMove(start, end, null));
        }

        return moves;
    }
}
