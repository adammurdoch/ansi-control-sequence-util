package net.rubygrapefruit.ansi.console;

import net.rubygrapefruit.ansi.Visitor;
import net.rubygrapefruit.ansi.token.*;

/**
 * Formats a stream of {@link net.rubygrapefruit.ansi.token.Token} into text, with control sequences converted to human-consumable placeholders, but does not interpret the sequences in any way. Normalizes '\r\n' sequences into a single '\n'.
 *
 * <p>Implementations are not thread-safe.</p>
 */
public class DiagnosticConsole implements Visitor {
    private final StringBuilder result = new StringBuilder();
    private Token last;

    @Override
    public void visit(Token token) {
        if (token instanceof Text) {
            Text text = (Text) token;
            result.append(text.getText());
        } else if (token instanceof ControlSequence) {
            ControlSequence controlSequence = (ControlSequence) token;
            result.append("{escape ").append(controlSequence.getSequence()).append('}');
        } else if (token instanceof NewLine) {
            if (last instanceof CarriageReturn) {
                result.replace(result.length() - 1, result.length(), "\n");
            } else {
                result.append('\n');
            }
        } else if (token instanceof CarriageReturn) {
            result.append('\r');
        } else {
            throw new UnsupportedOperationException("Unknown token: " + token);
        }
        last = token;
    }

    @Override
    public String toString() {
        return result.toString();
    }
}
