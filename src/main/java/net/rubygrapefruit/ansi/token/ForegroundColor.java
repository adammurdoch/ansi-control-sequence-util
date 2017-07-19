package net.rubygrapefruit.ansi.token;

import net.rubygrapefruit.ansi.TextColor;

public class ForegroundColor extends ControlSequence {
    public static final ForegroundColor DEFAULT = new ForegroundColor(TextColor.DEFAULT);

    private final TextColor color;

    private ForegroundColor(TextColor color) {
        this.color = color;
    }

    public static ForegroundColor of(TextColor color) {
        if (color == TextColor.DEFAULT) {
            return DEFAULT;
        }
        return new ForegroundColor(color);
    }

    public TextColor getColor() {
        return color;
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{foreground-color ");
        color.appendDiagnostics(builder);
        builder.append("}");
    }
}
