package chess.pieces;
import java.util.ArrayList;
import chess.*;

public class Pawn implements AbstractPiece {

    @Override
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition start) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        ChessPiece pawn = board.getPiece(start);
        int direction = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (direction == 1) ? 2 : 7;
        int promotionRow = (direction == 1) ? 8 : 1;

        ChessPosition forward1 = new ChessPosition(start.getRow() + direction, start.getColumn());

        if (forward1.insideBoard() && board.getPiece(forward1) == null) {
            moves.add(new ChessMove(start, forward1, null));

            ChessPosition forward2 = new ChessPosition(start.getRow() + 2 * direction, start.getColumn());
            if (start.getRow() == startRow && board.getPiece(forward2) == null) {
                moves.add(new ChessMove(start, forward2, null));
            }
        }

        ChessPosition leftCapture = new ChessPosition(start.getRow() + direction, start.getColumn() - 1);
        ChessPosition rightCapture = new ChessPosition(start.getRow() + direction, start.getColumn() + 1);

        if (leftCapture.insideBoard()) {
            ChessPiece leftPiece = board.getPiece(leftCapture);
            if (leftPiece != null && leftPiece.getTeamColor() != pawn.getTeamColor()) {
                moves.add(new ChessMove(start, leftCapture, null));
            }
        }
        if (rightCapture.insideBoard()) {
            ChessPiece rightPiece = board.getPiece(rightCapture);
            if (rightPiece != null && rightPiece.getTeamColor() != pawn.getTeamColor()) {
                moves.add(new ChessMove(start, rightCapture, null));
            }
        }

        ArrayList<ChessMove> finalMoves = new ArrayList<>();

        for (ChessMove move : moves) {
            if (move.getEndPosition().getRow() == promotionRow) {
                ChessPosition end = move.getEndPosition();

                finalMoves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
                finalMoves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
                finalMoves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
                finalMoves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
            } else {
                finalMoves.add(move);
            }
        }
        return finalMoves;
    }
}