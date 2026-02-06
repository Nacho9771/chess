package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private final ChessBoard board;
    private TeamColor teamTurn;
    private boolean finished;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();

        finished = false;
        teamTurn = TeamColor.WHITE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {return true;}
        if (!(obj instanceof ChessGame)) {return false;}

        ChessGame other = (ChessGame) obj;
        boolean sameBoard = board.equals(other.getBoard());
        boolean sameTurn  = teamTurn.equals(other.getTeamTurn());

        return sameBoard && sameTurn;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        if (board.getPiece(startPosition) == null) {return null;}

        ArrayList<ChessMove> moves = (ArrayList<ChessMove>) board.getPiece(startPosition).pieceMoves(board, startPosition);
        ArrayList<ChessMove> availableMoves = new ArrayList<>();
        ChessGame.TeamColor team = board.getPiece(startPosition).getTeamColor();

        for (int i = 0; i < moves.size(); ++i) {
            ChessGame game = new ChessGame();
            game.setBoard(board);
            ChessPiece.PieceType promotionPiece = moves.get(i).getPromotionPiece();
            ChessPiece piece = game.getBoard().getPiece(moves.get(i).getStartPosition());
            ChessPiece newPiece = promotionPiece == null ? piece : new ChessPiece(team, promotionPiece);

            game.getBoard().addPiece(moves.get(i).getEndPosition(), newPiece);
            game.getBoard().removePiece(moves.get(i).getStartPosition());
            if (!game.isInCheck(team)) {
                availableMoves.add(moves.get(i));
            }
        }
        return availableMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);

        if (piece == null) {
            throw new InvalidMoveException("Invalid move: no piece");
        }

        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Invalid move: " + move);
        }

        ArrayList<ChessMove> validMovesList = (ArrayList<ChessMove>) validMoves(start);
        boolean moveIsValid = false;

        for (ChessMove validMove : validMovesList) {
            if (validMove.equals(move)) {
                moveIsValid = true;
                break;
            }
        }

        if (!moveIsValid) {
            throw new InvalidMoveException("Invalid move: " + move);
        }

        ChessPiece pieceToPlace = piece;
        if (move.getPromotionPiece() != null) {
            pieceToPlace = new ChessPiece(
                    piece.getTeamColor(),
                    move.getPromotionPiece()
            );
        }
        pieceToPlace.flagAsMoved();
        board.addPiece(end, pieceToPlace);
        board.removePiece(start);

        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(ChessGame.TeamColor teamColor) {
        ChessPosition kingPosition = board.findKing(teamColor);
        TeamColor enemy = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        return board.isAttacked(kingPosition, enemy);
    }

    private boolean hasAnyValidMove(TeamColor teamColor) {
        ArrayList<ChessPosition> pieces = board.findAllPieces(teamColor);

        for (ChessPosition position : pieces) {
            if (!validMoves(position).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {return false;}
        return hasAnyValidMove(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {return false;}
        return hasAnyValidMove(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param newBoard the new board to use
     */
    public void setBoard(ChessBoard newBoard) {
        board.setBoard(newBoard);
        teamTurn = TeamColor.WHITE;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    public void finishGame() {
        finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

}
