package net.rubygrapefruit.ansi.token;

/**
 * An immutable parsed element of a byte stream.
 */
public abstract class Token {
    /**
     * Appends a diagnostic description of this token to the given target.
     */
    public abstract void appendDiagnostic(StringBuilder builder);

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        appendDiagnostic(builder);
        return builder.toString();
    }
}
