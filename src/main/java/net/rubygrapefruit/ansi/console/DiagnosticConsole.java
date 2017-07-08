package net.rubygrapefruit.ansi.console;

import net.rubygrapefruit.ansi.Visitor;
import net.rubygrapefruit.ansi.token.CarriageReturn;
import net.rubygrapefruit.ansi.token.NewLine;
import net.rubygrapefruit.ansi.token.Token;

/**
 * Formats a stream of {@link Token} instances into plain text, with control sequences converted to human-consumable placeholders. Does not interpret the sequences in any way. Normalizes '\r\n' sequences into a single '\n'.
 *
 * <p>This can be used as a parameter to {@link net.rubygrapefruit.ansi.AnsiParser#newParser(String, Visitor)} to format a stream of bytes.</p>
 *
 * <p>Implementations are not thread-safe.</p>
 */
public class DiagnosticConsole implements Visitor {
    private final StringBuilder result = new StringBuilder();
    private Token last;

    @Override
    public void visit(Token token) {
        if (token instanceof NewLine) {
            if (last instanceof CarriageReturn) {
                result.replace(result.length() - 4, result.length(), "\n");
            } else {
                result.append('\n');
            }
        } else {
            token.appendDiagnostic(result);
        }
        last = token;
    }

    @Override
    public String toString() {
        return result.toString();
    }
}
