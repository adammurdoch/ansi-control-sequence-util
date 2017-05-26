package net.rubygrapefruit.ansi.console;

import net.rubygrapefruit.ansi.Visitor;
import net.rubygrapefruit.ansi.token.CarriageReturn;
import net.rubygrapefruit.ansi.token.NewLine;
import net.rubygrapefruit.ansi.token.Text;
import net.rubygrapefruit.ansi.token.Token;

import java.util.LinkedList;
import java.util.List;

/**
 * A simple terminal emulator, that interprets a sequence of {@link Token}.
 */
public class AnsiConsole implements Visitor {
    private final LinkedList<RowImpl> rows = new LinkedList<RowImpl>();
    private int col;
    private RowImpl current;

    @Override
    public void visit(Token token) {
        if (current == null) {
            current = new RowImpl();
            rows.add(current);
        }
        if (token instanceof NewLine) {
            current = null;
            col = 0;
        } else if (token instanceof CarriageReturn) {
            col = 0;
        } else {
            col = current.insertAt(col, token);
        }
    }

    /**
     * Returns the rows display on the console, ordered from top-most to bottom-most.
     *
     * @return the rows.
     */
    public List<? extends Row> getRows() {
        return rows;
    }

    public interface Row {
        /**
         * Visits the contents of this row. Does not visit any end-of-line characters.
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
    }
}
