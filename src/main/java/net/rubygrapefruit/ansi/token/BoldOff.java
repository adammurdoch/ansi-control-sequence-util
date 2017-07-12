package net.rubygrapefruit.ansi.token;

public class BoldOff extends Token {
    public static final BoldOff INSTANCE = new BoldOff();

    private BoldOff() {
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{bold-off}");
    }
}
