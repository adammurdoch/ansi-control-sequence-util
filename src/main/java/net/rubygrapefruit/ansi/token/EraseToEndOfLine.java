package net.rubygrapefruit.ansi.token;

public class EraseToEndOfLine extends ControlSequence {
    public static final EraseToEndOfLine INSTANCE = new EraseToEndOfLine();

    private EraseToEndOfLine() {
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{clear-to-end-of-line}");
    }
}
