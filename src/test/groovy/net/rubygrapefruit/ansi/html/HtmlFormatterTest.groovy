package net.rubygrapefruit.ansi.html

import net.rubygrapefruit.ansi.token.BoldOff
import net.rubygrapefruit.ansi.token.BoldOn
import net.rubygrapefruit.ansi.token.CursorBackward
import net.rubygrapefruit.ansi.token.EraseInLine
import net.rubygrapefruit.ansi.token.ForegroundColor
import net.rubygrapefruit.ansi.token.NewLine
import net.rubygrapefruit.ansi.token.Text
import net.rubygrapefruit.ansi.token.UnrecognizedControlSequence
import spock.lang.Specification

class HtmlFormatterTest extends Specification {
    def formatter = new HtmlFormatter()

    def "formats empty content"() {
        expect:
        formatter.toHtml().contains("<pre></pre>")
    }

    def "formats text"() {
        expect:
        formatter.visit(new Text("123"))
        formatter.visit(NewLine.INSTANCE)
        formatter.visit(new Text("456"))
        formatter.toHtml().contains("<pre>123\n456</pre>")
    }

    def "formats bold text"() {
        expect:
        formatter.visit(new Text("123"))
        formatter.visit(BoldOn.INSTANCE)
        formatter.visit(new Text("456"))
        formatter.visit(BoldOff.INSTANCE)
        formatter.visit(new Text("789"))
        formatter.toHtml().contains("<pre>123<span class='ansi-bold'>456</span>789</pre>")
    }

    def "ignores bold control sequence that has no effect"() {
        expect:
        formatter.visit(BoldOff.INSTANCE)
        formatter.visit(BoldOff.INSTANCE)
        formatter.visit(new Text("123"))
        formatter.visit(BoldOn.INSTANCE)
        formatter.visit(BoldOff.INSTANCE)
        formatter.visit(new Text("456"))
        formatter.visit(BoldOn.INSTANCE)
        formatter.visit(BoldOn.INSTANCE)
        formatter.visit(BoldOff.INSTANCE)
        formatter.visit(BoldOn.INSTANCE)
        formatter.visit(new Text("789"))
        formatter.toHtml().contains("<pre>123456<span class='ansi-bold'>789</span></pre>")
    }

    def "formats foreground color"() {
        expect:
        formatter.visit(new Text("123"))
        formatter.visit(new ForegroundColor("red"))
        formatter.visit(new Text("456"))
        formatter.visit(new ForegroundColor("green"))
        formatter.visit(new Text("789"))
        formatter.visit(new ForegroundColor(null))
        formatter.visit(new Text("123"))
        formatter.toHtml().contains("<pre>123<span class='ansi-red'>456</span><span class='ansi-green'>789</span>123</pre>")
    }

    def "formats foreground color and bold"() {
        expect:
        formatter.visit(new ForegroundColor("red"))
        formatter.visit(new Text("123"))
        formatter.visit(BoldOn.INSTANCE)
        formatter.visit(new Text("456"))
        formatter.visit(new ForegroundColor("green"))
        formatter.visit(new Text("789"))
        formatter.visit(new ForegroundColor(null))
        formatter.visit(new Text("123"))
        formatter.visit(BoldOff.INSTANCE)
        formatter.visit(new Text("456"))
        formatter.toHtml().contains("<pre><span class='ansi-red'>123</span><span class='ansi-bold ansi-red'>456</span><span class='ansi-bold ansi-green'>789</span><span class='ansi-bold'>123</span>456</pre>")
    }

    def "formats control sequences"() {
        expect:
        formatter.visit(new CursorBackward(2))
        formatter.visit(new Text("456"))
        formatter.visit(NewLine.INSTANCE)
        formatter.visit(EraseInLine.INSTANCE)
        formatter.toHtml().contains("<pre><span class='ansi-sequence'>{cursor-backward 2}</span>456\n<span class='ansi-sequence'>{erase-in-line}</span></pre>")
    }

    def "formats unrecognized control sequences"() {
        expect:
        formatter.visit(new UnrecognizedControlSequence("[123m"))
        formatter.visit(new Text("456"))
        formatter.visit(NewLine.INSTANCE)
        formatter.visit(new UnrecognizedControlSequence("[5m"))
        formatter.toHtml().contains("<pre><span class='ansi-unknown-sequence'>{escape [123m}</span>456\n<span class='ansi-unknown-sequence'>{escape [5m}</span></pre>")
    }

    def "formats interleaved control sequences and text attributes"() {
        expect:
        formatter.visit(BoldOn.INSTANCE)
        formatter.visit(new Text("123"))
        formatter.visit(new CursorBackward(2))
        formatter.visit(new Text("456"))
        formatter.visit(new UnrecognizedControlSequence("[5m"))
        formatter.toHtml().contains("<pre><span class='ansi-bold'>123</span><span class='ansi-sequence'>{cursor-backward 2}</span><span class='ansi-bold'>456</span><span class='ansi-unknown-sequence'>{escape [5m}</span></pre>")
    }
}
