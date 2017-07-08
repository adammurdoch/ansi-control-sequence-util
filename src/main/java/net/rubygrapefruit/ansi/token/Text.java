package net.rubygrapefruit.ansi.token;

/**
 * A sequence of characters, other than control sequences and end-of-line characters.
 */
public class Text extends Token {
    private final String text;

    public Text(String text) {
        this.text = text;
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{text '").append(text).append("'}");
    }

    public String getText() {
        return text;
    }
}
