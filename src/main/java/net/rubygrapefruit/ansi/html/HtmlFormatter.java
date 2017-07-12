package net.rubygrapefruit.ansi.html;

import net.rubygrapefruit.ansi.Visitor;
import net.rubygrapefruit.ansi.token.CarriageReturn;
import net.rubygrapefruit.ansi.token.NewLine;
import net.rubygrapefruit.ansi.token.Text;
import net.rubygrapefruit.ansi.token.Token;

/**
 * Formats a stream of {@link Token} instances into HTML. Does not handle control sequences. To do so, use an instance of this class as a parameter to {@link net.rubygrapefruit.ansi.console.AnsiConsole#contents(Visitor)}.
 *
 * <p>Generates spans with the following classes:</p>
 * <ul>
 *     <li>{@code ansi-unknown-sequence}: an unrecognized sequence.</li>
 * </ul>
 */
public class HtmlFormatter implements Visitor {
    private final StringBuilder content = new StringBuilder();

    /**
     * Returns the current content as HTML.
     */
    public String toHtml() {
        return "<!DOCTYPE html>\n<html>\n<head>\n<meta charset='UTF-8'>\n<style>\npre { font-family: monospace; }\n.ansi-unknown-sequence { color: white; background: red; }</style>\n</head>\n<body>\n<pre>" + content + "</pre>\n</body>\n</html>";
    }

    @Override
    public void visit(Token token) {
        if (token instanceof Text) {
            Text text = (Text) token;
            content.append(text.getText());
        } else if (token instanceof NewLine) {
            content.append("\n");
        } else if (token instanceof CarriageReturn) {
            content.append("\r");
        } else {
            content.append("<span class='ansi-unknown-sequence'>");
            token.appendDiagnostic(content);
            content.append("</span>");
        }
    }
}
