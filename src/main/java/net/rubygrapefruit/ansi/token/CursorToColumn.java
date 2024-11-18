package net.rubygrapefruit.ansi.token;

public class CursorToColumn extends ControlSequence {
    private final int count;

    public CursorToColumn(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{cursor-to-col ").append(count).append("}");
    }
}
