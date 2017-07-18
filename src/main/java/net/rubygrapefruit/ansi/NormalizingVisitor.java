package net.rubygrapefruit.ansi;

import net.rubygrapefruit.ansi.token.BoldOff;
import net.rubygrapefruit.ansi.token.BoldOn;
import net.rubygrapefruit.ansi.token.ForegroundColor;
import net.rubygrapefruit.ansi.token.Token;

import java.util.Objects;

/**
 * A {@link Visitor} that normalizes a stream of {@link Token} instances, to remove unnecessary tokens.
 */
public class NormalizingVisitor implements Visitor {
    private final Visitor visitor;
    private boolean forwardedBold;
    private boolean bold;
    private TextColor forwardedColor = TextColor.DEFAULT;
    private TextColor color = TextColor.DEFAULT;

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
        } else {
            if (bold && !forwardedBold) {
                visitor.visit(BoldOn.INSTANCE);
                forwardedBold = true;
            } else if (!bold && forwardedBold) {
                visitor.visit(BoldOff.INSTANCE);
                forwardedBold = false;
            }
            if (!Objects.equals(color, forwardedColor)) {
                visitor.visit(ForegroundColor.of(color));
                forwardedColor = color;
            }
            visitor.visit(token);
        }
    }

    public void endStream() {
        if (forwardedBold) {
            visitor.visit(BoldOff.INSTANCE);
            forwardedBold = false;
        }
        if (forwardedColor != TextColor.DEFAULT) {
            visitor.visit(ForegroundColor.DEFAULT);
            forwardedColor = null;
        }
    }

    public static NormalizingVisitor of(Visitor visitor) {
        if (visitor instanceof NormalizingVisitor) {
            return (NormalizingVisitor) visitor;
        }
        return new NormalizingVisitor(visitor);
    }
}
