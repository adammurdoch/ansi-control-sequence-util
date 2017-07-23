package net.rubygrapefruit.ansi.console;

import net.rubygrapefruit.ansi.NormalizingVisitor;
import net.rubygrapefruit.ansi.TextAttributes;
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
    private TextAttributes attributes = TextAttributes.NORMAL;

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
            attributes = attributes.boldOn();
        } else if (token instanceof BoldOff) {
            attributes = attributes.boldOff();
        } else if (token instanceof ForegroundColor) {
            ForegroundColor color = (ForegroundColor) token;
            attributes = attributes.color(color.getColor());
        } else if (token instanceof BackgroundColor) {
            BackgroundColor color = (BackgroundColor) token;
            attributes = attributes.background(color.getColor());
        } else {
            col = rows.get(row).insertAt(col, token, attributes);
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
        NormalizingVisitor normalizingVisitor = NormalizingVisitor.of(visitor);
        for (int i = 0; i < rows.size(); i++) {
            RowImpl row = rows.get(i);
            if (i > 0) {
                normalizingVisitor.visit(NewLine.INSTANCE);
            }
            row.doVisit(normalizingVisitor);
        }
        normalizingVisitor.endStream();
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
        private final StringBuilder chars;
        private TextAttributes attributes;
        private Span next;

        Span(String text, TextAttributes attributes) {
            this.attributes = attributes;
            chars = new StringBuilder(text);
        }

        Span() {
            attributes = TextAttributes.NORMAL;
            chars = new StringBuilder();
        }

        void collectDetails(StringBuilder builder) {
            builder.append("{");
            if (attributes.isBold()) {
                builder.append("bold ");
            }
            if (!attributes.getColor().isDefault()) {
                builder.append("color: ").append(attributes.getColor()).append(" ");
            }
            if (!attributes.getBackground().isDefault()) {
                builder.append("background: ").append(attributes.getBackground()).append(" ");
            }
            builder.append("'").append(chars).append("'}");
            if (next != null) {
                next.collectDetails(builder);
            }
        }

        void visit(Visitor visitor) {
            if (chars.length() > 0) {
                if (attributes.isBold()) {
                    visitor.visit(BoldOn.INSTANCE);
                } else {
                    visitor.visit(BoldOff.INSTANCE);
                }
                visitor.visit(ForegroundColor.of(attributes.getColor()));
                visitor.visit(BackgroundColor.of(attributes.getBackground()));
                visitor.visit(new Text(chars.toString()));
            }
            if (next != null) {
                next.visit(visitor);
            }
        }

        void insertAt(int col, String text, TextAttributes attributes) {
            if (col > chars.length()) {
                // Insert beyond the end of this span
                if (next != null) {
                    // Insert into next span
                    next.insertAt(col - chars.length(), text, attributes);
                } else if (this.attributes.equals(TextAttributes.NORMAL)) {
                    // Pad this span
                    while (col > chars.length()) {
                        chars.append(' ');
                    }
                    if (attributes.equals(TextAttributes.NORMAL)) {
                        chars.append(text);
                    } else {
                        next = new Span(text, attributes);
                    }
                } else {
                    // Add padding as next span
                    next = new Span();
                    next.insertAt(col - chars.length(), text, attributes);
                }
            } else if (col == chars.length()) {
                // Insert at the end of this span
                if (next != null) {
                    next.insertAt(0, text, attributes);
                } else if (attributes.equals(this.attributes)) {
                    chars.append(text);
                } else {
                    if (chars.length() == 0) {
                        this.attributes = attributes;
                        chars.append(text);
                    } else {
                        next = new Span(text, attributes);
                    }
                }
            } else if (attributes.equals(this.attributes)) {
                // Overwrite with same attributes
                int replace = Math.min(text.length(), chars.length() - col);
                if (replace == text.length()) {
                    // Replace within this span
                    chars.replace(col, col + text.length(), text);
                } else {
                    // Append text to this span and remove from next span
                    chars.setLength(col);
                    chars.append(text);
                    if (next != null) {
                        next = next.remove(text.length() - replace);
                    }
                }
            } else {
                // Overwrite with different attributes
                int endPos = col + text.length();
                if (endPos < chars.length()) {
                    // Split this span
                    Span tail = new Span(chars.substring(endPos), this.attributes);
                    tail.next = next;
                    Span replaced = new Span(text, attributes);
                    replaced.next = tail;
                    chars.setLength(col);
                    next = replaced;
                } else {
                    // Replace the tail of this span and remove from next span
                    int remove = endPos - chars.length();
                    if (col == 0) {
                        chars.setLength(0);
                        this.attributes = attributes;
                        chars.append(text);
                        if (next != null) {
                            next = next.remove(remove);
                        }
                    } else {
                        chars.setLength(col);
                        Span replaced = new Span(text, attributes);
                        if (next != null) {
                            next = next.remove(remove);
                        }
                        replaced.next = next;
                        next = replaced;
                    }
                }
            }
        }

        private Span remove(int count) {
            if (count == 0) {
                return this;
            }
            if (count == chars.length()) {
                return next;
            }
            if (count < chars.length()) {
                chars.replace(0, count, "");
                return this;
            }
            if (next != null) {
                return next.remove(count - chars.length());
            }
            return null;
        }

        /**
         * Erase from pos to end of row
         */
        void eraseToEnd(int pos) {
            // If outside span and has next, delegate
            // If outside span and no next and normal, pad
            // If outside span and no next and not normal, add span
            // If inside span and normal, set length and append
            // If inside span and not normal, set length and add span
            if (pos < chars.length()) {
                // Inside this span, trim then add a blank char
                chars.setLength(pos);
                if (attributes.equals(TextAttributes.NORMAL)) {
                    chars.append(' ');
                    next = null;
                } else {
                    next = new Span();
                    next.eraseToEnd(0);
                }
            } else if (next != null) {
                // Outside this span, and this span is not the last span
                next.eraseToEnd(pos - chars.length());
            } else if (!attributes.equals(TextAttributes.NORMAL)) {
                next = new Span();
                next.eraseToEnd(pos - chars.length());
            } else {
                // Outside this and this span is the last span
                while (pos >= chars.length()) {
                    chars.append(' ');
                }
            }
        }

        void erase(int col) {
            attributes = TextAttributes.NORMAL;
            chars.setLength(col);
            for (int i = 0; i < col; i++) {
                chars.setCharAt(i, ' ');
            }
            next = null;
        }

        /**
         * Erase from start of this span to the given position (exclusive).
         */
        void eraseToStart(int pos) {
            if (pos <= chars.length()) {
                for (int i = 0; i < pos; i++) {
                    chars.setCharAt(i, ' ');
                }
                if (!attributes.equals(TextAttributes.NORMAL)) {
                    if (pos == chars.length()) {
                        attributes = TextAttributes.NORMAL;
                    } else {
                        Span replaced = new Span(chars.substring(pos, chars.length()), this.attributes);
                        chars.setLength(pos);
                        attributes = TextAttributes.NORMAL;
                        replaced.next = next;
                        next = replaced;
                    }
                }
            } else {
                int remove = pos - chars.length();
                chars.setLength(pos);
                attributes = TextAttributes.NORMAL;
                for (int i = 0; i < pos; i++) {
                    chars.setCharAt(i, ' ');
                }
                if (next != null) {
                    next = next.remove(remove);
                }
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
            NormalizingVisitor normalizingVisitor = NormalizingVisitor.of(visitor);
            doVisit(normalizingVisitor);
            normalizingVisitor.endStream();
            return visitor;
        }

        void doVisit(Visitor visitor) {
            first.visit(visitor);
        }

        int insertAt(int col, Token token, TextAttributes attributes) {
            if (token instanceof Text) {
                Text text = (Text) token;
                first.insertAt(col, text.getText(), attributes);
                return col + text.getText().length();
            }
            return col;
        }

        void erase(int col) {
            first.erase(col);
        }

        /**
         * Erase from col to start of row, including col.
         */
        void eraseToStart(int col) {
            first.eraseToStart(col + 1);
        }

        /**
         * Erase from col to end of row, including col.
         */
        void eraseToEnd(int col) {
            first.eraseToEnd(col);
        }
    }
}
