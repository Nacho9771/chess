package chess;

import java.util.Collection;
import java.util.List;
import chess.pieces.*;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    private boolean moved;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
        moved = false;
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

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {return true;}
        if (!(obj instanceof ChessPiece)) {return false;}

        ChessPiece other = (ChessPiece) obj;
        boolean sameColor = this.pieceColor.equals(other.pieceColor);
        boolean sameType = this.type.equals(other.type);

        return sameType && sameColor;
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
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        AbstractPiece calc = switch (type) {
            case KING -> new King();
            case QUEEN -> new Queen();
            case ROOK -> new Rook();
            case BISHOP -> new Bishop();
            case KNIGHT -> new Knight();
            case PAWN -> new Pawn();
        };

        if (calc == null) {
            return List.of();
        }

        return calc.pieceMoves(board, myPosition);
    }

    public void flagAsMoved() {
        moved = true;
    }

}
