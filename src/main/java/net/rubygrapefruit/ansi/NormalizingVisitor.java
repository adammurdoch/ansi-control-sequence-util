package net.rubygrapefruit.ansi;

import net.rubygrapefruit.ansi.token.*;

/**
 * A {@link Visitor} that normalizes a stream of {@link Token} instances, to remove unnecessary tokens.
 */
public class NormalizingVisitor implements Visitor {
    private final Visitor visitor;
    private boolean forwardedBold;
    private boolean bold;
    private TextColor forwardedColor = TextColor.DEFAULT;
    private TextColor color = TextColor.DEFAULT;
    private TextColor forwardedBackground = TextColor.DEFAULT;
    private TextColor background = TextColor.DEFAULT;

    private NormalizingVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    @Override
    public void visit(Token token) {
        if (token instanceof BoldOn) {
            bold = true;
        } else if (token instanceof BoldOff) {
            bold = false;
        } else if (token instanceof ForegroundColor) {
            ForegroundColor colorToken = (ForegroundColor) token;
            color = colorToken.getColor();
        } else if (token instanceof BackgroundColor) {
            BackgroundColor colorToken = (BackgroundColor) token;
            background = colorToken.getColor();
        } else {
            if (bold && !forwardedBold) {
                visitor.visit(BoldOn.INSTANCE);
                forwardedBold = true;
            } else if (!bold && forwardedBold) {
                visitor.visit(BoldOff.INSTANCE);
                forwardedBold = false;
            }
            if (!color.equals(forwardedColor)) {
                visitor.visit(ForegroundColor.of(color));
                forwardedColor = color;
            }
            if (!background.equals(forwardedBackground)) {
                visitor.visit(BackgroundColor.of(background));
                forwardedBackground = background;
            }
            visitor.visit(token);
        }
    }

    public void endStream() {
        if (forwardedBold) {
            visitor.visit(BoldOff.INSTANCE);
            forwardedBold = false;
        }
        if (!forwardedColor.isDefault()) {
            visitor.visit(ForegroundColor.DEFAULT);
            forwardedColor = null;
        }
        if (!forwardedBackground.isDefault()) {
            visitor.visit(BackgroundColor.DEFAULT);
            forwardedBackground = null;
        }
    }

    public static NormalizingVisitor of(Visitor visitor) {
        if (visitor instanceof NormalizingVisitor) {
            return (NormalizingVisitor) visitor;
        }
        return new NormalizingVisitor(visitor);
    }
}
