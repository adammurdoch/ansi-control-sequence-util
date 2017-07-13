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
    private boolean bold;

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
        } else if (token instanceof BoldOn) {
            bold = true;
        } else if (token instanceof BoldOff) {
            bold = false;
        } else {
            col = rows.get(row).insertAt(col, token, bold);
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

    private static class Span {
        private boolean bold;
        private final StringBuilder chars = new StringBuilder();
        private Span next;

        Span(String text, boolean bold) {
            chars.append(text);
            this.bold = bold;
        }

        Span() {
        }

        void collectDetails(StringBuilder builder) {
            builder.append("{");
            if (bold) {
                builder.append("bold ");
            }
            builder.append("'").append(chars).append("'}");
            if (next != null) {
                next.collectDetails(builder);
            }
        }

        void visit(Visitor visitor) {
            if (chars.length() > 0) {
                if (bold) {
                    visitor.visit(BoldOn.INSTANCE);
                }
                visitor.visit(new Text(chars.toString()));
                if (bold) {
                    visitor.visit(BoldOff.INSTANCE);
                }
            }
            if (next != null) {
                next.visit(visitor);
            }
        }

        void insertAt(int col, String text, boolean bold) {
            if (col > chars.length()) {
                if (next != null) {
                    next.insertAt(col - chars.length(), text, bold);
                } else if (!this.bold) {
                    while (col > chars.length()) {
                        chars.append(' ');
                    }
                    if (!bold) {
                        chars.append(text);
                    } else {
                        next = new Span(text, true);
                    }
                } else {
                    throw new UnsupportedOperationException();
                }
            } else if (col == chars.length()) {
                if (next != null) {
                    next.insertAt(0, text, bold);
                } else if (bold == this.bold) {
                    chars.append(text);
                } else if (chars.length() == 0) {
                    this.bold = bold;
                    chars.append(text);
                } else {
                    next = new Span(text, bold);
                }
            } else if (bold == this.bold) {
                int replace = Math.min(text.length(), chars.length() - col);
                if (replace == text.length()) {
                    chars.replace(col, col + text.length(), text);
                } else {
                    chars.setLength(col);
                    chars.append(text);
                    if (next != null) {
                        next = next.remove(text.length() - replace);
                    }
                }
            } else {
                // Different attributes
                if (col + text.length() < chars.length()) {
                    Span tail = new Span(chars.substring(col + text.length()), this.bold);
                    tail.next = next;
                    Span replaced = new Span(text, bold);
                    replaced.next = tail;
                    chars.setLength(col);
                    next = replaced;
                } else {
                    int remove = col + text.length() - chars.length();
                    chars.setLength(col);
                    Span replaced = new Span(text, bold);
                    if (next != null) {
                        next = next.remove(remove);
                    }
                    replaced.next = next;
                    next = replaced;
                }
            }
        }

        private Span remove(int count) {
            if (count == 0) {
                return this;
            }
            if (count < chars.length()) {
                chars.replace(0, count, "");
                return this;
            }
            if (next != null) {
                return next.remove(chars.length());
            }
            return null;
        }

        void eraseToEnd(int col) {
            if (col == chars.length()) {
                next = null;
            } else if (col < chars.length()) {
                chars.setLength(col);
                next = null;
            } else if (next != null) {
                next.eraseToEnd(col - chars.length());
            } else if (bold) {
                next = new Span();
                next.eraseToEnd(col - chars.length());
            } else {
                while (col > chars.length()) {
                    chars.append(' ');
                }
            }
        }

        void erase(int col) {
            bold = false;
            chars.setLength(col);
            for (int i = 0; i < col; i++) {
                chars.setCharAt(i, ' ');
            }
            next = null;
        }

        void eraseToStart(int col) {
            if (bold) {
                throw new UnsupportedOperationException();
            }
            if (col <= chars.length()) {
                for (int i = 0; i < col; i++) {
                    chars.setCharAt(i, ' ');
                }
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class RowImpl implements Row {
        private final Span first = new Span();

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            first.collectDetails(builder);
            return builder.toString();
        }

        @Override
        public <T extends Visitor> T visit(T visitor) {
            first.visit(visitor);
            return visitor;
        }

        int insertAt(int col, Token token, boolean bold) {
            if (token instanceof Text) {
                Text text = (Text) token;
                first.insertAt(col, text.getText(), bold);
                return col + text.getText().length();
            }
            return col;
        }

        void erase(int col) {
            first.erase(col);
        }

        void eraseToStart(int col) {
            first.eraseToStart(col);
        }

        void eraseToEnd(int col) {
            first.eraseToEnd(col);
        }
    }
}
