package net.rubygrapefruit.ansi.html

import net.rubygrapefruit.ansi.token.CursorBackward
import net.rubygrapefruit.ansi.token.EraseInLine
import net.rubygrapefruit.ansi.token.NewLine
import net.rubygrapefruit.ansi.token.Text
import spock.lang.Specification

class HtmlFormatterTest extends Specification {
    def formatter = new HtmlFormatter()

    def "formats text"() {
        expect:
        formatter.visit(new Text("123"))
        formatter.visit(NewLine.INSTANCE)
        formatter.visit(new Text("456"))
        formatter.toHtml().contains("<pre>123\n456</pre>")
    }

    def "formats control sequences"() {
        expect:
        formatter.visit(new CursorBackward(2))
        formatter.visit(new Text("456"))
        formatter.visit(NewLine.INSTANCE)
        formatter.visit(EraseInLine.INSTANCE)
        formatter.toHtml().contains("<pre><span class='ansi-unknown-sequence'>{cursor-backward 2}</span>456\n<span class='ansi-unknown-sequence'>{erase-in-line}</span></pre>")
    }
}
