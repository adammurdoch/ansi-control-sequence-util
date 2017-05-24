package net.rubygrapefruit.ansi;

/**
 * A sequence of characters, other than control sequences and end-of-line characters.
 */
public class Text extends Token {
    private final String text;

    public Text(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "[text '" + text + "']";
    }

    public String getText() {
        return text;
    }
}
