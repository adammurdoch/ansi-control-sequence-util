package net.rubygrapefruit.ansi.token;

public class BoldOn extends Token {
    public static final BoldOn INSTANCE = new BoldOn();

    private BoldOn() {
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{bold-on}");
    }
}
