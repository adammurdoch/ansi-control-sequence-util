package net.rubygrapefruit.ansi.html;

import net.rubygrapefruit.ansi.Visitor;
import net.rubygrapefruit.ansi.token.*;

import java.util.Objects;

/**
 * Formats a stream of {@link Token} instances into HTML. Handles text attribute control sequences but does not handle cursor control sequences. To do so, use an instance of this class as a parameter to {@link net.rubygrapefruit.ansi.console.AnsiConsole#contents(Visitor)}.
 *
 * <p>Generates spans with the following classes:</p>
 * <ul>
 *     <li>{@code ansi-bold}: bold text.</li>
 *     <li>{@code ansi-black}: black foreground.</li>
 *     <li>{@code ansi-red}: red foreground.</li>
 *     <li>{@code ansi-green}: green foreground.</li>
 *     <li>{@code ansi-yellow}: yellow foreground.</li>
 *     <li>{@code ansi-blue}: blue foreground.</li>
 *     <li>{@code ansi-magenta}: magenta foreground.</li>
 *     <li>{@code ansi-cyan}: cyan foreground.</li>
 *     <li>{@code ansi-white}: white foreground.</li>
 *     <li>{@code ansi-sequence}: a sequence that is recognized but not interpreted.</li>
 *     <li>{@code ansi-unknown-sequence}: an unrecognized sequence.</li>
 * </ul>
 */
public class HtmlFormatter implements Visitor {
    private final StringBuilder content = new StringBuilder();
    private boolean bold;
    private String foreground;
    private boolean spanHasContent;

    /**
     * Returns the current content as HTML.
     */
    public String toHtml() {
        endCurrentSpan();

        return "<!DOCTYPE html>\n<html>\n<head>\n<meta charset='UTF-8'>\n<style>\npre { font-family: monospace; }\n.ansi-bold { font-weight: bold; }\n.ansi-black { color: rgb(0,0,0); }\n.ansi-red { color: rgb(194,54,33); }\n.ansi-green { color: rgb(37,188,36); }\n.ansi-yellow { color: rgb(173,173,39); }\n.ansi-blue { color: rgb(73,46,225); }\n.ansi-magenta { color: rgb(211,56,211); }\n.ansi-cyan { color: rgb(51,187,200); }\n.ansi-white { color: rgb(203,204,205); }\n.ansi-unknown-sequence { color: white; background: red; }\n.ansi-sequence { color: black; background: #c0c0c0; }</style>\n</head>\n<body>\n<pre>" + content + "</pre>\n</body>\n</html>";
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
        } else if (token instanceof BoldOn) {
            if (bold) {
                return;
            }
            endCurrentSpan();
            bold = true;
        } else if (token instanceof BoldOff) {
            if (!bold) {
                return;
            }
            endCurrentSpan();
            bold = false;
        } else if (token instanceof ForegroundColor) {
            ForegroundColor foregroundColor = (ForegroundColor) token;
            if (Objects.equals(foreground, foregroundColor.getColorName())) {
                return;
            }
            endCurrentSpan();
            foreground = foregroundColor.getColorName();
        } else if (token instanceof UnrecognizedControlSequence) {
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
        if (spanHasContent && (bold || foreground != null)) {
            content.append("</span>");
        }
        spanHasContent = false;
    }

    private void appendText(String chars) {
        if (!spanHasContent) {
            if (bold && foreground != null) {
                content.append("<span class='ansi-bold ansi-").append(foreground).append("'>");
            } else if (bold) {
                content.append("<span class='ansi-bold'>");
            } else if (foreground != null) {
                content.append("<span class='ansi-").append(foreground).append("'>");
            }
        }
        content.append(chars);
        spanHasContent = true;
    }
}
