package net.rubygrapefruit.ansi;

import net.rubygrapefruit.ansi.tokens.Token;

public interface Visitor {
    void visit(Token token);
}
