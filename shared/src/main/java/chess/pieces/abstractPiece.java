package chess.pieces;
import java.util.Collection;
import chess.ChessMove;
import chess.ChessBoard;
import chess.ChessPosition;

/**
 * This acts as the base for all chess pieces
 */
public interface abstractPiece {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);
}
