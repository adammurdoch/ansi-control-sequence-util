package net.rubygrapefruit.ansi.console;

import net.rubygrapefruit.ansi.Visitor;
import net.rubygrapefruit.ansi.token.NewLine;
import net.rubygrapefruit.ansi.token.Token;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple terminal emulator, that interprets a sequence of {@link Token}.
 */
public class AnsiConsole implements Visitor {
    private final List<RowImpl> rows = new LinkedList<RowImpl>();
    private RowImpl current;

    @Override
    public void visit(Token token) {
        if (current == null) {
            current = new RowImpl();
            rows.add(current);
        }
        if (token instanceof NewLine) {
            current.hasNewLine = true;
            current = null;
        } else {
            current.tokens.add(token);
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
        private final List<Token> tokens = new ArrayList<>();
        private boolean hasNewLine;

        @Override
        public String toString() {
            return tokens.toString();
        }

        @Override
        public <T extends Visitor> T visit(T visitor) {
            for (Token token : tokens) {
                visitor.visit(token);
            }
            return visitor;
        }
    }
}
