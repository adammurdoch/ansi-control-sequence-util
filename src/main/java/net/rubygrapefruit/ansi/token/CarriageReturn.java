package net.rubygrapefruit.ansi.token;

public class CarriageReturn extends Token {
    public static final CarriageReturn INSTANCE = new CarriageReturn();

    private CarriageReturn() {
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{cr}");
    }

    @Override
    public String toString() {
        return "[cr]";
    }
}
