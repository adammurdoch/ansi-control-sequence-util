package net.rubygrapefruit.ansi.token;

public class CursorForward extends ControlSequence {
    private final int count;

    public CursorForward(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{cursor-forward ").append(count).append("}");
    }
}
