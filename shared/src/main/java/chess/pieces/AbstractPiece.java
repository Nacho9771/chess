package chess.pieces;

import java.util.ArrayList;
import chess.*;

/**
 * This acts as the base for all chess pieces
 */
public interface AbstractPiece {
    ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
}