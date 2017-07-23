package net.rubygrapefruit.ansi.token;

/**
 * Erase text from cursor to the end of the current line, including the character under the cursor. Erased text is filled with the current background color.
 */
public class EraseToEndOfLine extends ControlSequence {
    public static final EraseToEndOfLine INSTANCE = new EraseToEndOfLine();

    private EraseToEndOfLine() {
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{erase-to-end-of-line}");
    }
}
