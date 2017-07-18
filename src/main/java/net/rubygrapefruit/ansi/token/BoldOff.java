package net.rubygrapefruit.ansi.token;

public class BoldOff extends ControlSequence {
    public static final BoldOff INSTANCE = new BoldOff();

    private BoldOff() {
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{bold-off}");
    }
}
