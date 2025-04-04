package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;

/**
 * Represents a character-based frame used in a Command Line Interface.
 * <p>
 * This class allows storing and manipulating a grid of characters, supporting merging with other frames
 * and applying offsets to align elements properly within the CLI display.
 */
public class CLIFrame {
    public static final char INVISIBLE = ' ';

    private final int rows, columns;
    private final char[][] content;
    private final String[] contentAsLines;

    private int offset_row, offset_column;

    /**
     * Creates a CLI frame from an array of strings.
     *
     * @param lines The array of strings representing the frame's content.
     */
    public CLIFrame(String[] lines) {
        contentAsLines = lines;

        rows = lines.length;
        int tmpCols, cols = 0;
        for (int r = 0; r < rows; r++) {
            tmpCols = lines[r].length();
            if (tmpCols > cols) {
                cols = tmpCols;
            }
        }
        columns = cols;

        content = new char[rows][columns];
        for (int r = 0; r < rows; r++) {
            int len = lines[r].length();
            for (int c = 0; c < columns; c++) {
                if (c < len) {
                    content[r][c] = lines[r].charAt(c);
                } else {
                    content[r][c] = INVISIBLE;
                }
            }
        }

        resetOffset();
    }

    /**
     * Creates an empty CLI frame.
     */
    public CLIFrame() {
        this(new String[0]);
    }

    /**
     * Creates a new CLI frame by copying another frame.
     *
     * @param other The frame to copy.
     */
    public CLIFrame(CLIFrame other) {
        this(other.contentAsLines);
        offset_row = other.offset_row;
        offset_column = other.offset_column;
    }

    /**
     * Gets the number of rows in the frame.
     *
     * @return The number of rows.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the starting row index, including the offset.
     *
     * @return The first row index.
     */
    public int getFirstRowInclusive() {
        return offset_row;
    }

    /**
     * Gets the ending row index, excluding the offset.
     *
     * @return The last row index (exclusive).
     */
    public int getLastRowExclusive() {
        return rows + offset_row;
    }

    /**
     * Gets the number of columns in the frame.
     *
     * @return The number of columns.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Gets the starting column index, including the offset.
     *
     * @return The first column index.
     */
    public int getFirstColumnInclusive() {
        return offset_column;
    }

    /**
     * Gets the ending column index, excluding the offset.
     *
     * @return The last column index (exclusive).
     */
    public int getLastColumnExclusive() {
        return columns + offset_column;
    }

    /**
     * Retrieves the character at the specified position in the frame.
     *
     * @param row The row index.
     * @param col The column index.
     * @return The character at the given position, or {@link #INVISIBLE} if out of bounds.
     */
    public char getAt(int row, int col) {
        row -= offset_row;
        col -= offset_column;
        if (row < 0 || row >= rows || col < 0 || col >= columns) {
            return INVISIBLE;
        }
        return content[row][col];
    }

    /**
     * Gets the frame content as an array of lines.
     *
     * @return The content as an array of strings.
     */
    public String[] getContentAsLines() {
        return contentAsLines;
    }

    /**
     * Applies an offset to the frame.
     *
     * @param row The row offset to apply.
     * @param col The column offset to apply.
     */
    public void applyOffset(int row, int col) {
        offset_row += row;
        offset_column += col;
    }

    /**
     * Resets the frame offset to a specified position.
     *
     * @param row The row offset.
     * @param col The column offset.
     */
    public void resetOffset(int row, int col) {
        offset_row = row;
        offset_column = col;
    }

    /**
     * Resets the frame offset to (0,0).
     */
    public void resetOffset() {
        resetOffset(0, 0);
    }

    /**
     * Centers the frame around a given anchor point.
     *
     * @param frame  The frame to be centered.
     * @param anchor The anchor point.
     * @return The frame with the adjusted offset.
     */
    private static CLIFrame centerInAnchor(CLIFrame frame, AnchorPoint anchor) {
        int leftOffset = switch (anchor) {
            case TOP_LEFT, LEFT, BOTTOM_LEFT -> 0;
            case TOP, CENTER, BOTTOM -> -frame.getColumns() / 2;
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> -frame.getColumns();
        };
        int topOffset = switch (anchor) {
            case TOP_RIGHT, TOP, TOP_LEFT -> 0;
            case RIGHT, CENTER, LEFT -> -frame.getRows() / 2;
            case BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT -> -frame.getRows();
        };
        frame.applyOffset(topOffset, leftOffset);
        return frame;
    }

    /**
     * Merges this frame with another frame, aligning them according to specified anchor points.
     *
     * @param add The frame to merge.
     * @param selfAnchor The anchor point for this frame.
     * @param addAnchor The anchor point for the frame being added.
     * @return The merged frame.
     */
    public CLIFrame merge(CLIFrame add, AnchorPoint selfAnchor, AnchorPoint addAnchor) {
        CLIFrame baseFrame = centerInAnchor(this, selfAnchor);
        CLIFrame addFrame = centerInAnchor(add, addAnchor);

        int rowStart = Math.min(baseFrame.getFirstRowInclusive(), addFrame.getFirstRowInclusive());
        int rowEnd = Math.max(baseFrame.getLastRowExclusive(), addFrame.getLastRowExclusive());
        int colStart = Math.min(baseFrame.getFirstColumnInclusive(), addFrame.getFirstColumnInclusive());
        int colEnd = Math.max(baseFrame.getLastColumnExclusive(), addFrame.getLastColumnExclusive());

        // build lines the shortest possible to contain both frames
        String[] lines = new String[rowEnd - rowStart];
        StringBuilder lineBuilder;
        char overwrite;
        for (int row = rowStart; row < rowEnd; row++) {
            lineBuilder = new StringBuilder();
            for (int col = colStart; col < colEnd; col++) {
                overwrite = addFrame.getAt(row, col);
                // default: override the content with added frame, but if invisible there: draw base frame instead
                if (overwrite != CLIFrame.INVISIBLE) {
                    lineBuilder.append(overwrite);
                } else {
                    lineBuilder.append(baseFrame.getAt(row, col));
                }
            }
            lines[row - rowStart] = lineBuilder.toString();
        }

        return new CLIFrame(lines);
    }

    /**
     * Merges this frame with another frame with an additional offset.
     *
     * @param add The frame to merge.
     * @param selfAnchor The anchor point for this frame.
     * @param addAnchor The anchor point for the frame being added.
     * @param addOffsetRow The row offset for the added frame.
     * @param addOffsetColumn The column offset for the added frame.
     * @return The merged frame.
     */
    public CLIFrame merge(CLIFrame add, AnchorPoint selfAnchor, AnchorPoint addAnchor,
                          int addOffsetRow, int addOffsetColumn) {
        CLIFrame addRep = new CLIFrame(add);
        addRep.applyOffset(addOffsetRow, addOffsetColumn);
        return merge(addRep, selfAnchor, addAnchor);
    }

    /**
     * Merges this frame with another frame using default alignment.
     *
     * @param add The frame to merge.
     * @return The merged frame.
     */
    public CLIFrame merge(CLIFrame add) {
        return merge(add, AnchorPoint.TOP_LEFT, AnchorPoint.TOP_LEFT);
    }

    /**
     * Merges this frame with another frame in a specified direction.
     *
     * @param add The frame to merge.
     * @param direction The direction in which to merge the frames.
     * @return The merged frame.
     */
    public CLIFrame merge(CLIFrame add, Direction direction) {
        return switch (direction) {
            case EAST -> merge(add, AnchorPoint.TOP_RIGHT, AnchorPoint.TOP_LEFT);
            case NORTH -> merge(add, AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_LEFT);
            case WEST -> merge(add, AnchorPoint.TOP_LEFT, AnchorPoint.TOP_RIGHT);
            case SOUTH -> merge(add, AnchorPoint.BOTTOM_LEFT, AnchorPoint.TOP_LEFT);
        };
    }

    /**
     * Merges this CLI frame with another one in a specified direction, adding space between them.
     *
     * @param add The {@link CLIFrame} to merge with the current one.
     * @param direction The direction in which to merge the additional frame.
     * @param space The number of spaces to insert between the two frames.
     * @return A new {@link CLIFrame} representing the merged result.
     */
    public CLIFrame merge(CLIFrame add, Direction direction, int space) {
        return switch (direction) {
            case EAST -> merge(add, AnchorPoint.TOP_RIGHT, AnchorPoint.TOP_LEFT, 0, space);
            case NORTH -> merge(add, AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_LEFT, -space, 0);
            case WEST -> merge(add, AnchorPoint.TOP_LEFT, AnchorPoint.TOP_RIGHT, 0, -space);
            case SOUTH -> merge(add, AnchorPoint.BOTTOM_LEFT, AnchorPoint.TOP_LEFT, space, 0);
        };
    }

    @Override
    public String toString() {
        return String.join("\n", contentAsLines);
    }
}
