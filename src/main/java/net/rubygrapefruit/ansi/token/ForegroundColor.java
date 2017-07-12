package net.rubygrapefruit.ansi.token;

public class ForegroundColor extends Token {
    private final String colorName;

    public ForegroundColor(String colorName) {
        this.colorName = colorName;
    }

    public String getColorName() {
        return colorName;
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{foreground-color ").append(colorName).append("}");
    }
}
