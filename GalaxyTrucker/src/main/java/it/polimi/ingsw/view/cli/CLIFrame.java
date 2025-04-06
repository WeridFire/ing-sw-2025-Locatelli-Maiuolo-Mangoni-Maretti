package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents a character-based frame used in a Command Line Interface.
 * <p>
 * This class allows storing and manipulating a grid of characters, supporting merging with other frames
 * and applying offsets to align elements properly within the CLI display.
 */
public class CLIFrame implements Serializable {
    public static final char INVISIBLE = ' ';

    private final int rows;
    // Internal columns now equals logical columns * 3 (foreground, background, visible char)
    private final int columns;
    private final char[][] content;
    private final String[] contentAsLines;
    private boolean transparent = true;
    private int offset_row, offset_column;


    /**
     * @see #CLIFrame(String[])  CLIFrame
     * @param lines The array of strings representing the frame's content
     * @param transparent Wether the frame is transparent or not.
     */
    public CLIFrame(String[] lines, boolean transparent){
        this(lines);
        this.transparent = transparent;
    }


    /**
     * Creates a CLI frame from an array of strings.
     * Each visible character is preceded by a foreground and background color.
     *
     * By default both foreground and background are ANSI_RESET.
     *
     * @param lines The array of strings representing the frame's content.
     */
    public CLIFrame(String[] lines) {
        contentAsLines = lines;

        // Calculate rows and maximum logical columns (ignoring ANSI placeholders)
        rows = lines.length;
        int logicalCols = 0;
        int tmpCols;
        for (int r = 0; r < rows; r++) {
            tmpCols = ANSI.stripAnsi(lines[r]).length();
            if (tmpCols > logicalCols) {
                logicalCols = tmpCols;
            }
        }
        // Each cell occupies 3 internal slots (foreground, background, char)
        columns = logicalCols * 3;

        content = new char[rows][columns];
        for (int r = 0; r < rows; r++) {
            String line = lines[r];
            int len = line.length();
            // Start with default colors for foreground and background
            char currentForeground = ANSI.RESET.charAt(0);
            char currentBackground = ANSI.RESET.charAt(0);
            // writeIndex counts logical cells
            int writeIndex = 0;

            for (int c = 0; c < len; c++) {
                char ch = line.charAt(c);
                if (ANSI.isAnsi(ch)) {
                    // If it's an ANSI_RESET, reset both colors
                    if (ch == ANSI.RESET.charAt(0)) {
                        currentForeground = ANSI.RESET.charAt(0);
                        currentBackground = ANSI.RESET.charAt(0);
                    } else if (ANSI.isForeground(ch)) {
                        currentForeground = ch;
                    } else if (ANSI.isBackground(ch)) {
                        currentBackground = ch;
                    }
                } else {
                    // Before storing, ensure colors are in proper order:
                    // Foreground should be a foreground code and background a background code.
                    if (ANSI.isBackground(currentForeground) && ANSI.isForeground(currentBackground)) {
                        // swap if they are in wrong order
                        char temp = currentForeground;
                        currentForeground = currentBackground;
                        currentBackground = temp;
                    }
                    // Commit cell only if there is space for one logical cell
                    if (writeIndex < columns / 3) {
                        int pos = writeIndex * 3;
                        content[r][pos] = currentForeground;
                        content[r][pos + 1] = currentBackground;
                        content[r][pos + 2] = ch;
                        writeIndex++;
                    }
                }
            }
            // Fill remaining cells with default colors and INVISIBLE character
            while (writeIndex < columns / 3) {
                int pos = writeIndex * 3;
                content[r][pos] = ANSI.RESET.charAt(0);
                content[r][pos + 1] = ANSI.RESET.charAt(0);
                content[r][pos + 2] = INVISIBLE;
                writeIndex++;
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
        transparent = other.transparent;;
    }

    /**
     * Creates a CLI frame from a single line string.
     *
     * @param line The string representing the frame's content.
     */
    public CLIFrame(String line) {
        this(new String[] { line });
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
     * Gets the number of logical columns (each representing one visible character).
     *
     * @return The number of logical columns.
     */
    public int getColumns() {
        return columns / 3;
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
     * Gets the starting logical column index, including the offset.
     *
     * @return The first logical column index.
     */
    public int getFirstColumnInclusive() {
        return offset_column;
    }

    /**
     * Gets the ending logical column index, excluding the offset.
     *
     * @return The last logical column index (exclusive).
     */
    public int getLastColumnExclusive() {
        return getColumns() + offset_column;
    }

    /**
     * Retrieves the visible character at the specified logical position in the frame.
     *
     * @param row The logical row index.
     * @param col The logical column index.
     * @return The visible character at the given position, or {@link #INVISIBLE} if out of bounds.
     */
    public char getCharAt(int row, int col) {
        int logicalRow = row - offset_row;
        int logicalCol = col - offset_column;
        if (logicalRow < 0 || logicalRow >= rows || logicalCol < 0 || logicalCol >= getColumns()) {
            return INVISIBLE;
        }
        // Visible char is stored at slot index 2 of the cell.
        return content[logicalRow][logicalCol * 3 + 2];
    }

    /**
     * Retrieves the foreground color at the specified logical position in the frame.
     *
     * @param row The logical row index.
     * @param col The logical column index.
     * @return The foreground color placeholder at the given position, or ANSI_RESET if out of bounds.
     */
    public char getForegroundAt(int row, int col) {
        int logicalRow = row - offset_row;
        int logicalCol = col - offset_column;
        if (logicalRow < 0 || logicalRow >= rows || logicalCol < 0 || logicalCol >= getColumns()) {
            return ANSI.RESET.charAt(0);
        }
        return content[logicalRow][logicalCol * 3];
    }

    /**
     * Retrieves the background color at the specified logical position in the frame.
     *
     * @param row The logical row index.
     * @param col The logical column index.
     * @return The background color placeholder at the given position, or ANSI_RESET if out of bounds.
     */
    public char getBackgroundAt(int row, int col) {
        int logicalRow = row - offset_row;
        int logicalCol = col - offset_column;
        if (logicalRow < 0 || logicalRow >= rows || logicalCol < 0 || logicalCol >= getColumns()) {
            return ANSI.RESET.charAt(0);
        }
        return content[logicalRow][logicalCol * 3 + 1];
    }

    /**
     * Gets the frame content as the original array of strings.
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
     * When merging, for each logical cell:
     * - If the added frame's visible character is not INVISIBLE, its cell (foreground, background, char) is used.
     * - If the visible character is INVISIBLE but its foreground is not ANSI_RESET, the added frame’s colors (even with INVISIBLE)
     *   are used.
     * - Otherwise, the base frame's cell is used.
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

        // Build merged lines over logical cells
        String[] lines = new String[rowEnd - rowStart];
        StringBuilder lineBuilder;
        for (int row = rowStart; row < rowEnd; row++) {
            lineBuilder = new StringBuilder();
            for (int col = colStart; col < colEnd; col++) {
                char addVisible = addFrame.getCharAt(row, col);
                if (addVisible != INVISIBLE) {
                    // Use the cell from addFrame (foreground, background, and visible char)
                    lineBuilder.append(addFrame.getForegroundAt(row, col));
                    if(addFrame.getBackgroundAt(row, col) == ANSI.RESET.charAt(0)){
                        lineBuilder.append(baseFrame.getBackgroundAt(row, col));
                    }else{
                        lineBuilder.append(addFrame.getBackgroundAt(row, col));
                    }
                    lineBuilder.append(addVisible);
                } else {
                    // If the added cell is invisible but its foreground (or background) is not reset, keep its colors.
                    char addForeground = addFrame.getForegroundAt(row, col);
                    char addBackground = addFrame.getBackgroundAt(row, col);
                    if (addForeground != ANSI.RESET.charAt(0) && addBackground != ANSI.RESET.charAt(0)) {
                        lineBuilder.append(addForeground);
                        lineBuilder.append(addBackground);
                        lineBuilder.append(INVISIBLE);
                    } else {
                        // Otherwise, use the background frame's cell.
                        lineBuilder.append(baseFrame.getForegroundAt(row, col));
                        if(addBackground != ANSI.RESET.charAt(0)){
                            lineBuilder.append(addFrame.getBackgroundAt(row, col));
                        }else{
                            lineBuilder.append(baseFrame.getBackgroundAt(row, col));
                        }

                        if(addFrame.transparent || addBackground == ANSI.RESET.charAt(0)){
                            lineBuilder.append(baseFrame.getCharAt(row, col));
                        }else{
                            lineBuilder.append(addFrame.getCharAt(row, col));
                        }


                    }
                }
            }
            lines[row - rowStart] = lineBuilder.toString();
        }

        return new CLIFrame(lines, transparent);
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
     * @param add The CLIFrame to merge with the current one.
     * @param direction The direction in which to merge the additional frame.
     * @param space The number of logical cells (spaces) to insert between the two frames.
     * @return A new CLIFrame representing the merged result.
     */
    public CLIFrame merge(CLIFrame add, Direction direction, int space) {
        return switch (direction) {
            case EAST -> merge(add, AnchorPoint.RIGHT, AnchorPoint.LEFT, 0, space);
            case NORTH -> merge(add, AnchorPoint.TOP, AnchorPoint.BOTTOM, -space, 0);
            case WEST -> merge(add, AnchorPoint.LEFT, AnchorPoint.RIGHT, 0, -space);
            case SOUTH -> merge(add, AnchorPoint.BOTTOM, AnchorPoint.TOP, space, 0);
        };
    }

    @Override
    public String toString() {
        // Rebuild the frame from internal content (logical row by logical row)
        String[] lines = new String[rows];
        for (int r = 0; r < rows; r++) {
            StringBuilder sb = new StringBuilder();
            int logicalCells = getColumns();
            for (int c = 0; c < logicalCells; c++) {
                int pos = c * 3;
                sb.append(content[r][pos]);       // foreground
                sb.append(content[r][pos + 1]);     // background
                sb.append(content[r][pos + 2]);     // visible character
            }
            lines[r] = sb.toString();
        }
        return String.join("\n",
                Arrays.stream(lines)
                        .map(ANSI::applyColors)
                        .toArray(String[]::new));
    }
}
