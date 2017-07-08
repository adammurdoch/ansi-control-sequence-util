package net.rubygrapefruit.ansi.token;

public class NewLine extends Token {
    public static final NewLine INSTANCE = new NewLine();

    private NewLine() {
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{new-line}");
    }
}
