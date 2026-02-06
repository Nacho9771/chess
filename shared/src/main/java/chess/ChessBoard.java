package chess;

import java.util.ArrayList;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    ChessPiece[][] squares = new ChessPiece[8][8];
    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Removes a chess piece on the chessboard
     *
     * @param position The position to remove the piece from
     * @return nothing lol
     */
    public void removePiece(ChessPosition position) {
        squares[position.getRow() - 1][position.getColumn() - 1] = null;
    }

    @Override
    public int hashCode() {
        int code = 0;

        for (int row = 0; row < squares.length; row++) {
            for (int col = 0; col < squares[row].length; col++) {
                ChessPiece piece = squares[row][col];

                int pieceHash;
                if (piece == null) {
                    pieceHash = 71;
                } else {
                    pieceHash = piece.hashCode();
                }
                code += pieceHash * (row + 1) * (col + 1);
            }
        }
        return code;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {return true;}
        if (!(obj instanceof ChessBoard)) {return false;}

        ChessBoard other = (ChessBoard) obj;
        for (int row = 0; row < squares.length; row++) {
            for (int col = 0; col < squares[row].length; col++) {
                ChessPiece thisPiece = squares[row][col];
                ChessPiece otherPiece = other.squares[row][col];

                if (thisPiece == null && otherPiece == null) {continue;}
                if (thisPiece == null || otherPiece == null) {return false;}
                if (!thisPiece.equals(otherPiece)) {return false;}
            }
        }
        return true;
    }

    private ChessPiece copyPiece(ChessPiece oldPiece) {
        return new ChessPiece(oldPiece.getTeamColor(), oldPiece.getPieceType());
    }

    public void setBoard(ChessBoard newBoard) {
        squares = new ChessPiece[8][8];

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);

                ChessPiece oldPiece = newBoard.getPiece(pos);
                if (oldPiece == null) { continue; }

                ChessPiece newPiece = copyPiece(oldPiece);
                addPiece(pos, newPiece);
            }
        }
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        ChessPiece.PieceType[] backRank = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };

        for (int col = 1; col <= 8; col++) {
            addPiece(new ChessPosition(2, col), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, col), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));

            addPiece(new ChessPosition(1, col), new ChessPiece(ChessGame.TeamColor.WHITE, backRank[col - 1]));
            addPiece(new ChessPosition(8, col), new ChessPiece(ChessGame.TeamColor.BLACK, backRank[col - 1]));
        }
    }

    public ChessPosition findKing(ChessGame.TeamColor teamColor) {
        for (int i = 0; i < squares.length; ++i) {
            for (int j = 0; j < squares[i].length; ++j) {
                ChessPiece piece = getPiece(new ChessPosition(i+1,j+1));
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return new ChessPosition(i + 1,j + 1);
                }
            }
        }
        return null;
    }

    public boolean isAttacked(ChessPosition target, ChessGame.TeamColor attackingTeam) {
        for (ChessPosition piecePosition : findAllPieces(attackingTeam)) {
            ChessPiece piece = getPiece(piecePosition);

            for (ChessMove move : piece.pieceMoves(this, piecePosition)) {
                if (target.equals(move.getEndPosition())) {
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<ChessPosition> findAllPieces(ChessGame.TeamColor teamColor) {
        ArrayList<ChessPosition> positions = new ArrayList<>();

        for (int row = 0; row < squares.length; row++) {
            for (int col = 0; col < squares[row].length; col++) {
                addIfMatchingTeam(positions, row, col, teamColor);
            }
        }
        return positions;
    }

    private void addIfMatchingTeam(ArrayList<ChessPosition> positions, int row, int col, ChessGame.TeamColor teamColor) {
        ChessPosition position = new ChessPosition(row + 1, col + 1);
        ChessPiece piece = getPiece(position);

        if (piece != null && piece.getTeamColor() == teamColor) {
            positions.add(position);
        }
    }

    public void resetEnPassant() {
        for (int row = 0; row < squares.length; row++) {
            for (int col = 0; col < squares[row].length; col++) {
                ChessPosition position = new ChessPosition(row + 1, col + 1);
                ChessPiece piece = getPiece(position);
                if (piece != null) {
                    piece.setEnPassantable(false);
                }
            }
        }
    }

}
