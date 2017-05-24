package net.rubygrapefruit.ansi;

import net.rubygrapefruit.ansi.token.Token;

public interface Visitor {
    void visit(Token token);
}
