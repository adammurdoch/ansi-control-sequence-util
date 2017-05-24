package net.rubygrapefruit.ansi.tokens;

public class ControlSequence extends Token {
    private final String sequence;

    public ControlSequence(String sequence) {
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return "[control-sequence '" + sequence + "']";
    }

    public String getSequence() {
        return sequence;
    }
}
