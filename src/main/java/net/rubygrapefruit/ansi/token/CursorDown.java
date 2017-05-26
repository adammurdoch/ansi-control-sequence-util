package net.rubygrapefruit.ansi.token;

public class CursorDown extends ControlSequence {
    private final int count;

    public CursorDown(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{cursor-down ").append(count).append("}");
    }
}
