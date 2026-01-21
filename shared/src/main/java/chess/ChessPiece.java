package chess;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * @return the correct end moves for Bishops
     */
    public Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece bishop = board.getPiece(myPosition);

        int[][] directions = {
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] dir : directions) {
            int row = myPosition.getRow() + dir[0];
            int col = myPosition.getColumn() + dir[1];

            while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPosition end = new ChessPosition(row, col);
                ChessPiece target = board.getPiece(end);

                if (target == null) {
                    moves.add(new ChessMove(myPosition, end, null));
                } else {
                    if (target.getTeamColor() != bishop.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, end, null));
                    }
                    break;
                }

                row += dir[0];
                col += dir[1];
            }
        }
        return moves;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() ==  PieceType.BISHOP) {
            return bishopMoves(board, myPosition);
        }
        return List.of();
    }
}
