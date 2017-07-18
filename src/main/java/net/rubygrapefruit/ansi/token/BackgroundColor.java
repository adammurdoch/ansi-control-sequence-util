package net.rubygrapefruit.ansi.token;

import net.rubygrapefruit.ansi.TextColor;

public class BackgroundColor extends ControlSequence {
    public static final BackgroundColor DEFAULT = new BackgroundColor(TextColor.DEFAULT);

    private final TextColor color;

    private BackgroundColor(TextColor color) {
        this.color = color;
    }

    public static BackgroundColor of(TextColor color) {
        if (color == TextColor.DEFAULT) {
            return DEFAULT;
        }
        return new BackgroundColor(color);
    }

    public TextColor getColor() {
        return color;
    }

    @Override
    public void appendDiagnostic(StringBuilder builder) {
        builder.append("{background-color ");
        if (color.isBright()) {
            builder.append("bright ");
        }
        builder.append(color.getName());
        builder.append("}");
    }
}
