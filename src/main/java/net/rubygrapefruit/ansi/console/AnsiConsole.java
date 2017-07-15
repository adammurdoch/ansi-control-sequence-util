package net.rubygrapefruit.ansi.console;

import net.rubygrapefruit.ansi.NormalizingVisitor;
import net.rubygrapefruit.ansi.Visitor;
import net.rubygrapefruit.ansi.token.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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
            attributes = attributes.color(color.getColorName());
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
            if (attributes.bold) {
                builder.append("bold ");
            }
            if (attributes.color != null) {
                builder.append(attributes.color).append(" ");
            }
            builder.append("'").append(chars).append("'}");
            if (next != null) {
                next.collectDetails(builder);
            }
        }

        void visit(Visitor visitor) {
            if (chars.length() > 0) {
                if (attributes.bold) {
                    visitor.visit(BoldOn.INSTANCE);
                } else {
                    visitor.visit(BoldOff.INSTANCE);
                }
                visitor.visit(new ForegroundColor(attributes.color));
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

        void eraseToEnd(int col) {
            if (col == chars.length()) {
                next = null;
            } else if (col < chars.length()) {
                chars.setLength(col);
                next = null;
            } else if (next != null) {
                next.eraseToEnd(col - chars.length());
            } else if (!attributes.equals(TextAttributes.NORMAL)) {
                next = new Span();
                next.eraseToEnd(col - chars.length());
            } else {
                while (col > chars.length()) {
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

        void eraseToStart(int col) {
            if (col == 0) {
                return;
            }
            if (col <= chars.length()) {
                for (int i = 0; i < col; i++) {
                    chars.setCharAt(i, ' ');
                }
                if (!attributes.equals(TextAttributes.NORMAL)) {
                    if (col == chars.length()) {
                        attributes = TextAttributes.NORMAL;
                    } else {
                        Span replaced = new Span(chars.substring(col, chars.length()), this.attributes);
                        chars.setLength(col);
                        attributes = TextAttributes.NORMAL;
                        replaced.next = next;
                        next = replaced;
                    }
                }
            } else {
                int remove = col - chars.length();
                chars.setLength(col);
                attributes = TextAttributes.NORMAL;
                for (int i = 0; i < col; i++) {
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

        void eraseToStart(int col) {
            first.eraseToStart(col);
        }

        void eraseToEnd(int col) {
            first.eraseToEnd(col);
        }
    }

    /**
     * Immutable text attributes.
     */
    static class TextAttributes {
        final boolean bold;
        final String color;

        static final TextAttributes NORMAL = new TextAttributes(false, null);
        static final TextAttributes BOLD = new TextAttributes(true, null);

        private TextAttributes(boolean bold, String color) {
            this.bold = bold;
            this.color = color;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            TextAttributes other = (TextAttributes) obj;
            return other.bold == bold && Objects.equals(other.color, color);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(color) ^ (bold ? 31 : 1);
        }

        TextAttributes boldOn() {
            if (bold) {
                return this;
            }
            if (color == null) {
                return BOLD;
            }
            return new TextAttributes(true, color);
        }

        TextAttributes boldOff() {
            if (!bold) {
                return this;
            }
            if (color == null) {
                return NORMAL;
            }
            return new TextAttributes(false, color);
        }

        TextAttributes color(String color) {
            if (Objects.equals(this.color, color)) {
                return this;
            }
            if (color == null && !bold) {
                return NORMAL;
            }
            return new TextAttributes(bold, color);
        }
    }
}
