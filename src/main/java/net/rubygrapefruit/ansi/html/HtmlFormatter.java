package net.rubygrapefruit.ansi.html;

import net.rubygrapefruit.ansi.Visitor;
import net.rubygrapefruit.ansi.token.NewLine;
import net.rubygrapefruit.ansi.token.Text;
import net.rubygrapefruit.ansi.token.Token;

/**
 * Formats a stream of {@link Token} instances into HTML. Does not handle control sequences. To do so, use an instance of this class as a parameter to {@link net.rubygrapefruit.ansi.console.AnsiConsole#contents(Visitor)}.
 */
public class HtmlFormatter implements Visitor {
    private final StringBuilder content = new StringBuilder();

    /**
     * Returns the current content as HTML.
     */
    public String toHtml() {
        return "<!DOCTYPE html>\n<html>\n<head>\n<meta charset='UTF-8'>\n<style>\npre { font-family: monospace; }\n</style>\n</head>\n<body>\n<pre>" + content + "</pre>\n</body>\n</html>";
    }

    @Override
    public void visit(Token token) {
        if (token instanceof Text) {
            Text text = (Text) token;
            content.append(text.getText());
        } else if (token instanceof NewLine) {
            content.append("\n");
        } else {
            token.appendDiagnostic(content);
        }
    }
}
