package net.rubygrapefruit.ansi.token;

public class CursorUp extends ControlSequence {
    private final int count;

    public CursorUp(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{cursor-up ").append(count).append("}");
    }
}
