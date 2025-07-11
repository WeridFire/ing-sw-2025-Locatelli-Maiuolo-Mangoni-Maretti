package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a character-based frame used in a Command Line Interface.
 * <p>
 * This class allows storing and manipulating a grid of characters, supporting merging with other frames
 * and applying offsets to align elements properly within the CLI display.
 */
public class CLIFrame implements Serializable {
    public static final char INVISIBLE = ' ';
    private static final short ANSI_INVISIBLE = 0;

    private final int rows, columns;
    private final char[][] content;
    private final short[][] foregroundANSI;
    private final short[][] backgroundANSI;
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
            tmpCols = ANSI.Helper.stripAnsi(lines[r]).length();
            if (tmpCols > cols) {
                cols = tmpCols;
            }
        }
        columns = cols;

        content = new char[rows][columns];
        foregroundANSI = new short[rows][columns];
        backgroundANSI = new short[rows][columns];
        short fg, bg;

        for (int r = 0; r < rows; r++) {
            List<String> line = ANSI.Helper.splitAnsiAndUnicode(lines[r]);

            int c = 0;
            fg = ANSI_INVISIBLE;
            bg = ANSI_INVISIBLE;

            for (String ch : line) {
                Integer code = ANSI.Helper.ansiToCode(ch);
                if (code != null) {
                    if (ch.equals(ANSI.RESET)) {
                        fg = ANSI_INVISIBLE;
                        bg = ANSI_INVISIBLE;
                    }
                    else if (ANSI.isForeground(ch)) {
                        fg = code.shortValue();
                    }
                    else if (ANSI.isBackground(ch)) {
                        bg = code.shortValue();
                    }
                    // else: unhandled ANSI code
                }
                else {
                    for (char realChar : ch.toCharArray()) {
                        content[r][c] = realChar;
                        foregroundANSI[r][c] = fg;
                        backgroundANSI[r][c] = bg;
                        c++;
                    }
                }
            }

            for (int cLeft = c; cLeft < columns; cLeft++) {
                content[r][cLeft] = INVISIBLE;
                foregroundANSI[r][cLeft] = ANSI_INVISIBLE;
                backgroundANSI[r][cLeft] = ANSI_INVISIBLE;
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
     * Creates a CLI frame from a single line string.
     *
     * @param line The string representing the frame's content.
     */
    public CLIFrame(String line) {
        this(new String[] {line});
    }

    /**
     * @return {@code true} if this frame has no content, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return contentAsLines.length == 0;
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


    private short getBackgroundAt(int row, int col) {
        row -= offset_row;
        col -= offset_column;
        if (row < 0 || row >= rows || col < 0 || col >= columns) {
            return ANSI_INVISIBLE;
        }
        return backgroundANSI[row][col];
    }

    private short getForegroundAt(int row, int col) {
        row -= offset_row;
        col -= offset_column;
        if (row < 0 || row >= rows || col < 0 || col >= columns) {
            return ANSI_INVISIBLE;
        }
        return foregroundANSI[row][col];
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

        // note: stored original offset to restore it at the end to not change the argument content
        int prevAddRowOffset = add.offset_row;
        int prevAddColOffset = add.offset_column;
        CLIFrame addFrame = centerInAnchor(add, addAnchor);

        int rowStart = Math.min(baseFrame.getFirstRowInclusive(), addFrame.getFirstRowInclusive());
        int rowEnd = Math.max(baseFrame.getLastRowExclusive(), addFrame.getLastRowExclusive());
        int colStart = Math.min(baseFrame.getFirstColumnInclusive(), addFrame.getFirstColumnInclusive());
        int colEnd = Math.max(baseFrame.getLastColumnExclusive(), addFrame.getLastColumnExclusive());

        // build lines the shortest possible to contain both frames
        String[] lines = new String[rowEnd - rowStart];
        StringBuilder lineBuilder;
        char visibleChar;
        short lastForeground = -1, lastBackground = -1;
        short fg, bg;

        for (int row = rowStart; row < rowEnd; row++) {
            lineBuilder = new StringBuilder();
            for (int col = colStart; col <= colEnd; col++) {  // included end-point for intrinsic ANSI.RESET

                // get info
                visibleChar = addFrame.getAt(row, col);
                // default: override the content with added frame, but if invisible there: draw base frame instead
                bg = addFrame.getBackgroundAt(row, col);
                fg = addFrame.getForegroundAt(row, col);

                if (bg == CLIFrame.ANSI_INVISIBLE) {  // keep the added background if present
                    bg = baseFrame.getBackgroundAt(row, col);
                    if (visibleChar == CLIFrame.INVISIBLE) {
                        fg = baseFrame.getForegroundAt(row, col);
                        visibleChar = baseFrame.getAt(row, col);
                    }
                }

                // MAYBE ONE IS RESET AND THE OTHER NOT: problem -> do in order
                // apply colors if changed
                if (bg != lastBackground) {
                    lineBuilder.append(ANSI.Helper.codeToAnsi(bg));
                    lastBackground = bg;
                    if (bg == CLIFrame.ANSI_INVISIBLE) {
                        lineBuilder.append(ANSI.Helper.codeToAnsi(fg));
                    }
                }
                if (fg != lastForeground) {
                    lineBuilder.append(ANSI.Helper.codeToAnsi(fg));
                    lastForeground = fg;
                    if (fg == CLIFrame.ANSI_INVISIBLE) {
                        lineBuilder.append(ANSI.Helper.codeToAnsi(bg));
                    }
                }

                if (col < colEnd) {
                    // append the visible character
                    lineBuilder.append(visibleChar);
                }
            }
            lines[row - rowStart] = lineBuilder.toString();
        }

        // reset offset of add
        addFrame.resetOffset(prevAddRowOffset, prevAddColOffset);
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
        add.applyOffset(addOffsetRow, addOffsetColumn);
        CLIFrame result = merge(add, selfAnchor, addAnchor);
        add.applyOffset(-addOffsetRow, -addOffsetColumn);
        return result;
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
            case EAST -> merge(add, AnchorPoint.RIGHT, AnchorPoint.LEFT);
            case NORTH -> merge(add, AnchorPoint.TOP, AnchorPoint.BOTTOM);
            case WEST -> merge(add, AnchorPoint.LEFT, AnchorPoint.RIGHT);
            case SOUTH -> merge(add, AnchorPoint.BOTTOM, AnchorPoint.TOP);
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
            case EAST -> merge(add, AnchorPoint.RIGHT, AnchorPoint.LEFT, 0, space);
            case NORTH -> merge(add, AnchorPoint.TOP, AnchorPoint.BOTTOM, -space, 0);
            case WEST -> merge(add, AnchorPoint.LEFT, AnchorPoint.RIGHT, 0, -space);
            case SOUTH -> merge(add, AnchorPoint.BOTTOM, AnchorPoint.TOP, space, 0);
        };
    }

    /**
     * Virtually applies a colored background to this frame and return the result.
     * <p>
     * Any part with an already colored background is not changed;
     * for all the parts with a transparent background: it changes in the specified color.
     *
     * @param backgroundFill The color to fill the background with.
     * @return A new {@link CLIFrame} representing the painted result,
     * or a copy of this {@link CLIFrame} if {@code backgroundFill} is not {@link ANSI} background.
     * @implNote {@link ANSI#RESET} is not a background but does nothing -> no need to create more than a copy
     */
    public CLIFrame paintBackground(String backgroundFill) {
        if (!ANSI.isBackground(backgroundFill)) {
            return new CLIFrame(this);
        }
        String emptyLine = backgroundFill + " ".repeat(columns);
        String[] frame = Collections.nCopies(rows, emptyLine).toArray(String[]::new);
        return new CLIFrame(frame).merge(this);
    }

    /**
     * Virtually applies a colored foreground to this frame and return the result.
     * <p>
     * Any part with an already colored foreground is not changed;
     * for all the parts with a non-set foreground: it changes in the specified color.
     *
     * @param foregroundFill The color to fill the foreground with.
     * @return A new {@link CLIFrame} representing the painted result,
     * or a copy of this {@link CLIFrame} if {@code foregroundFill} is not {@link ANSI} foreground.
     * @implNote {@link ANSI#RESET} is not a foreground but does nothing -> no need to create more than a copy
     */
    public CLIFrame paintForeground(String foregroundFill) {
        if (!ANSI.isForeground(foregroundFill)) {
            return new CLIFrame(this);
        }
        String[] frame = new String[rows];
        for (int i = 0; i < rows; i++) {
            frame[i] = contentAsLines[i].replace(ANSI.RESET, ANSI.RESET + foregroundFill);
            frame[i] = foregroundFill + frame[i] + ANSI.RESET;
        }
        return new CLIFrame(frame);
    }

    /**
     * Adds a {@link CLIFrame} to a grid of CLIFrames, organizing them in rows such that each row does not
     * exceed the specified maximum width.
     * <p>
     * If the new frame cannot fit in the last row due to width constraints, a new row is added to the grid.
     *
     * @param grid The current grid of CLIFrames, represented as a list of rows (each a list of CLIFrames).
     *             Can be {@code null}, in which case a new grid is created.
     * @param newFrame The new {@link CLIFrame} to be added.
     * @param horizontalSpacing The number of spaces to insert between frames in the same row.
     * @param maxWidth The maximum allowed width of a row before wrapping to a new row.
     * @return The updated grid with the new frame added, either to an existing row or a new one.
     */
    public static List<List<CLIFrame>> addInFramesGrid(List<List<CLIFrame>> grid, CLIFrame newFrame,
                                               int horizontalSpacing, int maxWidth) {
        if (newFrame == null || newFrame.isEmpty()) {
            return grid;
        }

        if (grid == null) {
            grid = new ArrayList<>();
        }
        List<CLIFrame> lastRow;
        try {
            lastRow = grid.getLast();
        } catch (NoSuchElementException e) {
            lastRow = new ArrayList<>();
            grid.add(lastRow);
        }

        if (!lastRow.isEmpty()) {
            // calculate if new frame would fit in the last row
            int totalWidth = lastRow.getFirst().getColumns();
            for (int i = 1; i < lastRow.size(); i++) {
                totalWidth += horizontalSpacing + lastRow.get(i).getColumns();
            }
            totalWidth += horizontalSpacing + newFrame.getColumns();
            if (totalWidth > maxWidth) {
                // does not fit -> new row
                lastRow = new ArrayList<>();
                grid.add(lastRow);
            }
        }

        lastRow.add(newFrame);
        grid.removeLast();
        grid.add(lastRow);
        return grid;
    }


    public static CLIFrame fromFramesGrid(List<List<CLIFrame>> grid, int horizontalSpacing, int verticalSpacing) {
        if (grid == null) {
            return new CLIFrame();
        }
        int currentRow = 0;
        CLIFrame totalFrame = new CLIFrame();
        for (List<CLIFrame> row : grid) {
            int maxHeight = 0;
            int currentCol = 0;
            for (CLIFrame cell : row) {
                if (cell.getRows() > maxHeight) {
                    maxHeight = cell.getRows();
                }

                totalFrame = totalFrame.merge(cell, AnchorPoint.TOP_LEFT, AnchorPoint.TOP_LEFT, currentRow, currentCol);
                currentCol += cell.getColumns() + horizontalSpacing;
            }
            currentRow += maxHeight + verticalSpacing;
        }
        return totalFrame;
    }


    /**
     * Copy this frame creating a new one, which will have all its content fit the specified width, creating new lines
     * (with the specified indentation quantity) while the content of a line would overflow the dimension.
     * @param maxWidth The maximum width for the content. Must be at least 1.
     * @param indentation The number of invisible characters to append at the beginning of each new line created due
     *                    to overflow problems. Must be less than {@code maxWidth}.
     *                    Should be at least 0, except fot exotic results
     *                    (new line starts before the original one if less than 0).
     * @param align An anchor point representing where to align the text in the provided width.
     *              Can be {@link AnchorPoint#LEFT}, {@link AnchorPoint#CENTER} or {@link AnchorPoint#RIGHT}.
     * @return A new {@link CLIFrame} that fits within {@code maxWidth} number of columns.
     * @throws IllegalArgumentException if {@code maxWidth < 1} or if {@code maxWidth < indentation}
     */
    public CLIFrame wrap(int maxWidth, int indentation, AnchorPoint align) {
        // checks on illegal arguments
        if (maxWidth < 1) {
            throw new IllegalArgumentException("maxWidth must be at least 1 column wide.");
        }
        if (indentation >= maxWidth) {
            throw new IllegalArgumentException("indentation must be less than maxWidth.");
        }
        // adjust alignment
        final float alignDisplacementMultiplier = switch (align) {
            case LEFT, TOP_LEFT, BOTTOM_LEFT -> 0;
            case CENTER, TOP, BOTTOM -> 0.5f;
            case RIGHT, TOP_RIGHT, BOTTOM_RIGHT -> 1f;
        };
        // variables
        List<String> contentLines = new ArrayList<>(contentAsLines.length);  // minimum: all the previous lines
        String indentationString = String.valueOf(INVISIBLE).repeat(indentation);
        // process all the lines in this frame
        for (String line : contentAsLines) {
            StringBuilder lineBuilder = new StringBuilder();
            StringBuilder ansiModifiers = new StringBuilder();  // to keep track of the colors when breaking line
            int colsLeft = maxWidth;
            // process each code point in the line as a character
            for (String lineChar : ANSI.Helper.splitAnsiAndUnicode(line)) {
                if (ANSI.Helper.ansiToCode(lineChar) == null) {  // it's not ANSI
                    // -> it's actually visible -> count as column(s)
                    int charLen = lineChar.length();
                    colsLeft -= charLen;  // try to fit the character in this line
                    if (colsLeft < 0) {  // this character does NOT fit in the width
                        // create a new line
                        contentLines.add(lineBuilder.toString());
                        lineBuilder = new StringBuilder(ansiModifiers).append(indentationString);
                        colsLeft = maxWidth - indentation - charLen;
                    }
                } else {
                    ansiModifiers.append(lineChar);
                }
                // append this character to the line
                lineBuilder.append(lineChar);
            }
            // at the end: append the processed line as a new line
            int lineOccupiedSpace = ANSI.Helper.stripAnsi(lineBuilder.toString()).length();
            int alignDisplacement = (int) ((maxWidth - lineOccupiedSpace) * alignDisplacementMultiplier);
            contentLines.add(
                    String.valueOf(INVISIBLE).repeat(alignDisplacement)
                    + lineBuilder
                    + String.valueOf(INVISIBLE).repeat(maxWidth - lineOccupiedSpace - alignDisplacement)
            );
        }
        return new CLIFrame(contentLines.toArray(new String[0]));
    }


    @Override
    public String toString() {
        return String.join("\n", contentAsLines);
    }
}