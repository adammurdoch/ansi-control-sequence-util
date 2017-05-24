package net.rubygrapefruit.ansi;

import java.nio.charset.Charset;

public class AnsiParser {
    /**
     * Parses the given bytes into a stream of {@link Token} instances.
     *
     * @param bytes The output
     * @param charset The charset that the text has been encoded using
     * @param visitor The visitor to receive the tokens
     */
    public void parse(byte[] bytes, String charset, Visitor visitor) {
        try {
            Charset encoding = Charset.forName(charset);
            int pos = 0;
            while (pos < bytes.length) {
                if (bytes[pos] == 27 && bytes[pos + 1] == '[') {
                    int startSequence = pos + 2;
                    int endToken = startSequence;
                    while (bytes[endToken] >= '0' && bytes[endToken] <= '9') {
                        endToken++;
                    }
                    if (bytes[endToken] == ';') {
                        endToken++;
                    }
                    while (bytes[endToken] >= '0' && bytes[endToken] <= '9') {
                        endToken++;
                    }
                    if (bytes[endToken] >= 'a' && bytes[endToken] <= 'z' || bytes[endToken] >= 'A' && bytes[endToken] <= 'Z') {
                        endToken++;
                    }
                    visitor.visit(new ControlSequence(new String(bytes, startSequence, endToken - startSequence, encoding)));
                    pos = endToken;
                    continue;
                }

                int endToken = pos;
                while (endToken < bytes.length && bytes[endToken] != 27) {
                    endToken++;
                }
                String sequence = new String(bytes, pos, endToken - pos, encoding);
                split(sequence, visitor);
                pos = endToken;
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not parse input", e);
        }
    }

    private void split(String string, Visitor visitor) {
        int pos = 0;
        while (pos < string.length()) {
            if (string.charAt(pos) == '\n') {
                visitor.visit(new NewLine());
                pos++;
            } else if (string.charAt(pos) == '\r') {
                visitor.visit(new CarriageReturn());
                pos++;
            } else {
                int endToken = pos;
                while (endToken < string.length() && string.charAt(endToken) != '\n' && string.charAt(endToken) != '\r') {
                    endToken++;
                }
                visitor.visit(new Text(string.substring(pos, endToken)));
                pos = endToken;
            }
        }
    }
}
