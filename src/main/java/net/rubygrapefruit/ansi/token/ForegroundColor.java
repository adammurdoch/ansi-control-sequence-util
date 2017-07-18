package net.rubygrapefruit.ansi.token;

import net.rubygrapefruit.ansi.TextColor;

public class ForegroundColor extends Token {
    private final TextColor color;

    public ForegroundColor(TextColor color) {
        this.color = color;
    }

    public static final ForegroundColor DEFAULT = new ForegroundColor(TextColor.DEFAULT);

    public TextColor getColor() {
        return color;
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{foreground-color ");
        if (color.isBright()) {
            builder.append("bright ");
        }
        builder.append(color.getName());
        builder.append("}");
    }
}
