package net.rubygrapefruit.ansi;

import net.rubygrapefruit.ansi.token.Token;

/**
 * Receives a stream of {@link Token} instances.
 */
public interface Visitor {
    void visit(Token token);
}
