package net.rubygrapefruit.ansi.token;

/**
 * Erase text from cursor to the start of the current line, including the character under the cursor. Fill erased text with the current background color.
 */
public class EraseToBeginningOfLine extends ControlSequence {
    public static final EraseToBeginningOfLine INSTANCE = new EraseToBeginningOfLine();

    private EraseToBeginningOfLine() {
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{erase-to-beginning-of-line}");
    }
}
