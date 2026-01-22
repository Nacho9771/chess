package chess.pieces;

import java.util.ArrayList;
import chess.*;

public class Pawn implements AbstractPiece {

    @Override
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        int forward = board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;

        ChessPosition forwardPosition = new ChessPosition(myPosition.getRow() + forward, myPosition.getColumn());
        if (forwardPosition.insideBoard() && board.getPiece(forwardPosition) == null) {
            possibleMoves.add(new ChessMove(myPosition, forwardPosition, null));
        }

        ChessPosition forward2Position = new ChessPosition(myPosition.getRow() + 2 * forward, myPosition.getColumn());
        if (myPosition.getRow() == (forward == 1 ? 2 : 7) && forward2Position.insideBoard()) {
            if (board.getPiece(forwardPosition) == null && board.getPiece(forward2Position) == null) {
                possibleMoves.add(new ChessMove(myPosition, forward2Position, null));
            }
        }

        ChessPosition leftAttack = new ChessPosition(myPosition.getRow() + forward, myPosition.getColumn() - 1);
        ChessPosition rightAttack = new ChessPosition(myPosition.getRow() + forward, myPosition.getColumn() + 1);

        if (leftAttack.insideBoard() && board.getPiece(leftAttack) != null &&
                board.getPiece(myPosition).getTeamColor() != board.getPiece(leftAttack).getTeamColor()) {
            possibleMoves.add(new ChessMove(myPosition, leftAttack, null));
        }

        if (rightAttack.insideBoard() && board.getPiece(rightAttack) != null &&
                board.getPiece(myPosition).getTeamColor() != board.getPiece(rightAttack).getTeamColor()) {
            possibleMoves.add(new ChessMove(myPosition, rightAttack, null));
        }

        ArrayList<ChessMove> finalMoves = new ArrayList<>();
        for (ChessMove move : possibleMoves) {
            if (move.getEndPosition().getRow() == (forward == 1 ? 8 : 1)) {
                ChessPosition start = move.getStartPosition();
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