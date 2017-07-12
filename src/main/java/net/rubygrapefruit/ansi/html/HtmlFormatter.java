package net.rubygrapefruit.ansi.html;

import net.rubygrapefruit.ansi.Visitor;
import net.rubygrapefruit.ansi.token.*;

/**
 * Formats a stream of {@link Token} instances into HTML. Handles text attribute control sequences but does not handle cursor control sequences. To do so, use an instance of this class as a parameter to {@link net.rubygrapefruit.ansi.console.AnsiConsole#contents(Visitor)}.
 *
 * <p>Generates spans with the following classes:</p>
 * <ul>
 *     <li>{@code ansi-bold}: bold text.</li>
 *     <li>{@code ansi-sequence}: a sequence that is recognized but not interpreted.</li>
 *     <li>{@code ansi-unknown-sequence}: an unrecognized sequence.</li>
 * </ul>
 */
public class HtmlFormatter implements Visitor {
    private final StringBuilder content = new StringBuilder();
    private boolean bold;
    private boolean spanHasContent;

    /**
     * Returns the current content as HTML.
     */
    public String toHtml() {
        endCurrentSpan();

        return "<!DOCTYPE html>\n<html>\n<head>\n<meta charset='UTF-8'>\n<style>\npre { font-family: monospace; }\n.ansi-bold { font-weight: bold; }\n.ansi-unknown-sequence { color: white; background: red; }\n.ansi-sequence { color: black; background: #c0c0c0; }</style>\n</head>\n<body>\n<pre>" + content + "</pre>\n</body>\n</html>";
    }

    @Override
    public void visit(Token token) {
        if (token instanceof Text) {
            Text text = (Text) token;
            appendText(text.getText());
        } else if (token instanceof NewLine) {
            appendText("\n");
        } else if (token instanceof CarriageReturn) {
            appendText("\r");
        } else if (token instanceof BoldOn){
            if (bold) {
                return;
            }
            bold = true;
            spanHasContent = false;
        } else if (token instanceof BoldOff){
            endCurrentSpan();
            bold = false;
        } else if (token instanceof UnrecognizedControlSequence){
            endCurrentSpan();
            content.append("<span class='ansi-unknown-sequence'>");
            token.appendDiagnostic(content);
            content.append("</span>");
        } else {
            endCurrentSpan();
            content.append("<span class='ansi-sequence'>");
            token.appendDiagnostic(content);
            content.append("</span>");
        }
    }

    private void endCurrentSpan() {
        if (spanHasContent && bold) {
            content.append("</span>");
            spanHasContent = false;
        }
    }

    private void appendText(String chars) {
        if (!spanHasContent && bold) {
            content.append("<span class='ansi-bold'>");
        }
        content.append(chars);
        spanHasContent = true;
    }
}
