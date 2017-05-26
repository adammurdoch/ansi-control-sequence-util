package net.rubygrapefruit.ansi.token;

public class CursorBackward extends ControlSequence {
    private final int count;

    public CursorBackward(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{cursor-backward ").append(count).append("}");
    }
}
