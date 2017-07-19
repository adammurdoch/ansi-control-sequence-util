package net.rubygrapefruit.ansi;

/**
 * Immutable text attributes.
 */
public class TextAttributes {
    private final boolean bold;
    private final TextColor color;
    private final TextColor background;

    public static final TextAttributes NORMAL = new TextAttributes(false, TextColor.DEFAULT, TextColor.DEFAULT);
    public static final TextAttributes BOLD = new TextAttributes(true, TextColor.DEFAULT, TextColor.DEFAULT);

    private TextAttributes(boolean bold, TextColor color, TextColor background) {
        this.bold = bold;
        this.color = color;
        this.background = background;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        TextAttributes other = (TextAttributes) obj;
        return other.bold == bold && other.color.equals(color) && other.background.equals(background);
    }

    @Override
    public int hashCode() {
        return color.hashCode() ^ background.hashCode() ^ (bold ? 31 : 1);
    }

    public boolean isBold() {
        return bold;
    }

    public TextColor getColor() {
        return color;
    }

    public TextColor getBackground() {
        return background;
    }

    /**
     * Returns a copy of these attributes with bold text enabled.
     */
    public TextAttributes boldOn() {
        if (bold) {
            return this;
        }
        if (color.isDefault() && background.isDefault()) {
            return BOLD;
        }
        return new TextAttributes(true, color, background);
    }

    /**
     * Returns a copy of these attributes with bold text disabled.
     */
    public TextAttributes boldOff() {
        if (!bold) {
            return this;
        }
        if (color.isDefault() && background.isDefault()) {
            return NORMAL;
        }
        return new TextAttributes(false, color, background);
    }

    /**
     * Returns a copy of these attributes with the given foreground color.
     */
    public TextAttributes color(TextColor color) {
        if (this.color.equals(color)) {
            return this;
        }
        if (color.isDefault() && background.isDefault() && !bold) {
            return NORMAL;
        }
        return new TextAttributes(bold, color, background);
    }

    /**
     * Returns a copy of these attributes with the given background color.
     */
    public TextAttributes background(TextColor background) {
        if (this.background.equals(background)) {
            return this;
        }
        if (color.isDefault() && background.isDefault() && !bold) {
            return NORMAL;
        }
        return new TextAttributes(bold, color, background);
    }
}
