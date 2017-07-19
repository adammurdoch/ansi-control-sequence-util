package net.rubygrapefruit.ansi.html;

import net.rubygrapefruit.ansi.TextAttributes;
import net.rubygrapefruit.ansi.TextColor;
import net.rubygrapefruit.ansi.Visitor;
import net.rubygrapefruit.ansi.token.*;

/**
 * Formats a stream of {@link Token} instances into HTML. Handles text attribute control sequences but does not handle cursor control sequences. To do so, use an instance of this class as a parameter to {@link net.rubygrapefruit.ansi.console.AnsiConsole#contents(Visitor)}.
 *
 * <p>Generates spans with the following classes:</p>
 * <ul>
 *     <li>{@code ansi-bold}: bold text.</li>
 *     <li>{@code ansi-<color>}: foreground color.</li>
 *     <li>{@code ansi-bright-<color>}: bright foreground color.</li>
 *     <li>{@code ansi-<color>-bg}: background color.</li>
 *     <li>{@code ansi-bright-<color>-bg}: background color.</li>
 *     <li>{@code ansi-sequence}: a sequence that is recognized but not interpreted.</li>
 *     <li>{@code ansi-unknown-sequence}: an unrecognized sequence.</li>
 * </ul>
 */
public class HtmlFormatter implements Visitor {
    private final StringBuilder content = new StringBuilder();
    private TextAttributes attributes = TextAttributes.NORMAL;
    private boolean spanHasContent;

    public HtmlFormatter() {
        content.append("<!DOCTYPE html>\n<head>\n<meta charset='UTF-8'>\n<style>");
        content.append("pre { font-family: monospace; font-size: 11pt; }\n");
        content.append(".ansi-bold { font-weight: bold; }\n");
        content.append(".ansi-unknown-sequence { color: white; background: red; }\n");
        content.append(".ansi-sequence { color: black; background: #c0c0c0; }\n");
        appendColorStyle(content, TextColor.BLACK, "rgb(0,0,0)", "rgb(129,131,131)");
        appendColorStyle(content, TextColor.RED, "rgb(194,54,33)", "rgb(252,57,31)");
        appendColorStyle(content, TextColor.GREEN, "rgb(37,188,3)", "rgb(49,231,34)");
        appendColorStyle(content, TextColor.YELLOW, "rgb(173,173,39)", "rgb(234,236,35)");
        appendColorStyle(content, TextColor.BLUE, "rgb(73,46,225)", "rgb(88,51,255)");
        appendColorStyle(content, TextColor.MAGENTA, "rgb(211,56,211)", "rgb(249,53,248)");
        appendColorStyle(content, TextColor.CYAN, "rgb(51,187,200)", "rgb(20,240,240)");
        appendColorStyle(content, TextColor.WHITE, "rgb(203,204,205)", "rgb(233,235,235)");
        content.append("</style>\n</head>\n<body>\n<pre>");
    }

    private void appendColorStyle(StringBuilder content, TextColor color, String cssValue, String brightCssValue) {
        content.append(".ansi-").append(color.getName()).append(" { color: ").append(cssValue).append("; }\n");
        content.append(".ansi-bright-").append(color.getName()).append(" { color: ").append(brightCssValue).append("; }\n");
        content.append(".ansi-").append(color.getName()).append("-bg { background: ").append(cssValue).append("; }\n");
        content.append(".ansi-bright-").append(color.getName()).append("-bg { background: ").append(brightCssValue).append("; }\n");
    }

    /**
     * Returns the current content as HTML.
     */
    public String toHtml() {
        endCurrentSpan();
        return content + "</pre>\n</body>\n</html>";
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
            if (attributes.isBold()) {
                return;
            }
            endCurrentSpan();
            attributes = attributes.boldOn();
        } else if (token instanceof BoldOff) {
            if (!attributes.isBold()) {
                return;
            }
            endCurrentSpan();
            attributes = attributes.boldOff();
        } else if (token instanceof ForegroundColor) {
            ForegroundColor foregroundColor = (ForegroundColor) token;
            if (attributes.getColor().equals(foregroundColor.getColor())) {
                return;
            }
            endCurrentSpan();
            attributes = attributes.color(foregroundColor.getColor());
        } else if (token instanceof BackgroundColor) {
            BackgroundColor backgroundColor = (BackgroundColor) token;
            if (attributes.getBackground().equals(backgroundColor.getColor())) {
                return;
            }
            endCurrentSpan();
            attributes = attributes.background(backgroundColor.getColor());
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
        if (spanHasContent && !attributes.equals(TextAttributes.NORMAL)) {
            content.append("</span>");
        }
        spanHasContent = false;
    }

    private void appendText(String chars) {
        if (!spanHasContent && !attributes.equals(TextAttributes.NORMAL)) {
            content.append("<span class='");
            boolean hasClass = false;
            if (attributes.isBold()) {
                content.append("ansi-bold");
                hasClass = true;
            }
            if (!attributes.getColor().isDefault()) {
                if (!hasClass) {
                    hasClass = true;
                } else {
                    content.append(' ');
                }
                appendColorClass(content, attributes.getColor());
            }
            if (!attributes.getBackground().isDefault()) {
                if (hasClass) {
                    content.append(' ');
                }
                appendColorClass(content, attributes.getBackground());
                content.append("-bg");
            }
            content.append("'>");
        }
        content.append(chars);
        spanHasContent = true;
    }

    private void appendColorClass(StringBuilder content, TextColor foreground) {
        content.append("ansi-");
        if (foreground.isBright()) {
            content.append("bright-");
        }
        content.append(foreground.getName());
    }
}
