package net.rubygrapefruit.ansi.token;

public class NewLine extends Token {
    public static final NewLine INSTANCE = new NewLine();

    private NewLine() {
    }

    @Override
    public String toString() {
        return "[new-line]";
    }
}
