package net.rubygrapefruit.ansi.token;

public class EraseToBeginningOfLine extends ControlSequence {
    public static final EraseToBeginningOfLine INSTANCE = new EraseToBeginningOfLine();

    private EraseToBeginningOfLine() {
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{erase-to-beginning-of-line}");
    }
}
