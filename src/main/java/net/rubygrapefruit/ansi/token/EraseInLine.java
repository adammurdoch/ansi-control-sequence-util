package net.rubygrapefruit.ansi.token;

public class EraseInLine extends ControlSequence {
    public static final EraseInLine INSTANCE = new EraseInLine();

    private EraseInLine() {
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{clear-line}");
    }
}
