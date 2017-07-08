package net.rubygrapefruit.ansi.console;

import net.rubygrapefruit.ansi.Visitor;
import net.rubygrapefruit.ansi.token.*;

import java.util.LinkedList;
import java.util.List;

/**
 * A simple terminal emulator that interprets a stream of {@link Token} instances.
 *
 * <p>This can be used as a parameter to {@link net.rubygrapefruit.ansi.AnsiParser#newParser(String, Visitor)} to interpret a stream of bytes.</p>
 *
 * To query the contents of the console you can use the {@link #getRows()} or {@link #contents(Visitor)} methods.
 */
public class AnsiConsole implements Visitor {
    private final LinkedList<RowImpl> rows = new LinkedList<RowImpl>();
    private int col;
    private int row;

    public AnsiConsole() {
        rows.add(new RowImpl());
    }

    @Override
    public String toString() {
        return "{console row: " + row + " col: " + col + " rows: " + rows + "}";
    }

    @Override
    public void visit(Token token) {
        if (token instanceof NewLine) {
            row++;
            col = 0;
            if (row >= rows.size()) {
                rows.add(new RowImpl());
            }
        } else if (token instanceof CarriageReturn) {
            col = 0;
        } else if (token instanceof CursorUp) {
            row = Math.max(0, row - ((CursorUp) token).getCount());
        } else if (token instanceof CursorDown) {
            row += ((CursorDown) token).getCount();
            while (row >= rows.size()) {
                rows.add(new RowImpl());
            }
        } else if (token instanceof CursorBackward) {
            col = Math.max(0, col - ((CursorBackward) token).getCount());
        } else if (token instanceof CursorForward) {
            col += ((CursorForward) token).getCount();
        } else if (token instanceof EraseInLine) {
            rows.get(row).erase(col);
        } else if (token instanceof EraseToBeginningOfLine) {
            rows.get(row).eraseToStart(col);
        } else if (token instanceof EraseToEndOfLine) {
            rows.get(row).eraseToEnd(col);
        } else {
            col = rows.get(row).insertAt(col, token);
        }
    }

    /**
     * Returns the current contents of the console, arranged as rows ordered from top-most to bottom-most.
     *
     * @return the rows.
     */
    public List<? extends Row> getRows() {
        return rows;
    }

    /**
     * Writes the current contents of the console to the given visitor.
     *
     * @return the visitor.
     */
    public <T extends Visitor> T contents(T visitor) {
        for (int i = 0; i < rows.size(); i++) {
            RowImpl row = rows.get(i);
            if (i > 0) {
                visitor.visit(NewLine.INSTANCE);
            }
            row.visit(visitor);
        }
        return visitor;
    }

    public interface Row {
        /**
         * Visits the contents of this row. Does not include any end-of-line characters.
         *
         * @return the visitor.
         */
        <T extends Visitor> T visit(T visitor);
    }

    private static class RowImpl implements Row {
        private final StringBuilder chars = new StringBuilder();

        @Override
        public String toString() {
            return chars.toString();
        }

        @Override
        public <T extends Visitor> T visit(T visitor) {
            visitor.visit(new Text(chars.toString()));
            return visitor;
        }

        int insertAt(int col, Token token) {
            if (token instanceof Text) {
                Text text = (Text) token;
                while (col > chars.length()) {
                    chars.append(' ');
                }
                int replace = Math.min(chars.length() - col, text.getText().length());
                if (replace > 0) {
                    chars.replace(col, col + replace, text.getText().substring(0, replace));
                    if (replace == text.getText().length()) {
                        return col + replace;
                    }
                    if (replace < text.getText().length()) {
                        chars.append(text.getText().substring(replace));
                    }
                } else {
                    chars.append(text.getText());
                }
                return chars.length();
            }
            return col;
        }

        void erase(int col) {
            chars.setLength(col);
            eraseToStart(col);
        }

        void eraseToStart(int col) {
            for (int i = 0; i < col; i++) {
                chars.setCharAt(i, ' ');
            }
        }

        void eraseToEnd(int col) {
            chars.setLength(col);
        }
    }
}
