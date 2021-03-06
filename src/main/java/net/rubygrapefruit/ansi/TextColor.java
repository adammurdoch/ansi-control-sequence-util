package net.rubygrapefruit.ansi;

/**
 * An immutable text color.
 */
public class TextColor {
    private final String name;
    private final boolean bright;

    public static final TextColor DEFAULT = new TextColor(null, false);
    public static final TextColor BLACK = new TextColor("black", false);
    public static final TextColor BRIGHT_BLACK = new TextColor("black", true);
    public static final TextColor RED = new TextColor("red", false);
    public static final TextColor BRIGHT_RED = new TextColor("red", true);
    public static final TextColor GREEN = new TextColor("green", false);
    public static final TextColor BRIGHT_GREEN = new TextColor("green", true);
    public static final TextColor YELLOW = new TextColor("yellow", false);
    public static final TextColor BRIGHT_YELLOW = new TextColor("yellow", true);
    public static final TextColor BLUE = new TextColor("blue", false);
    public static final TextColor BRIGHT_BLUE = new TextColor("blue", true);
    public static final TextColor MAGENTA = new TextColor("magenta", false);
    public static final TextColor BRIGHT_MAGENTA = new TextColor("magenta", true);
    public static final TextColor CYAN = new TextColor("cyan", false);
    public static final TextColor BRIGHT_CYAN = new TextColor("cyan", true);
    public static final TextColor WHITE = new TextColor("white", false);
    public static final TextColor BRIGHT_WHITE = new TextColor("white", true);

    private TextColor(String name, boolean bright) {
        this.name = name;
        this.bright = bright;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        appendDiagnostics(result);
        return result.toString();
    }

    public void appendDiagnostics(StringBuilder builder) {
        if (bright) {
            builder.append("bright ");
        }
        if (name != null) {
            builder.append(name);
        } else {
            builder.append("default");
        }
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }

    public String getName() {
        return name;
    }

    public boolean isBright() {
        return bright;
    }
}
