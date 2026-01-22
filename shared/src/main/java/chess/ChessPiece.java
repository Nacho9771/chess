package chess;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import chess.pieces.*;

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
    private boolean enPassantable;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
        moved = false;
        enPassantable = false;
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

    public void flagAsMoved() {
        moved = true;
    }

    public boolean ifMoved() {
        return moved;
    }

    public void setEnPassantable(boolean ep) {
        enPassantable = ep;
    }

    public boolean isEnPassantable() {
        return enPassantable;
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
}
