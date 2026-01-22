package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null || getClass() != obj.getClass()) { return false; }
        ChessPosition that = (ChessPosition) obj;
        return row == that.getRow() && col == that.getColumn();
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return col;
    }

    public String toString() {
        return String.format("[%d,%d]", row, col);
    }

    public boolean insideBoard() {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

}
