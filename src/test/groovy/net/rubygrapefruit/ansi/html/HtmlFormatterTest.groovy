package net.rubygrapefruit.ansi.html

import net.rubygrapefruit.ansi.TextColor
import net.rubygrapefruit.ansi.token.*
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

    def "escapes text"() {
        expect:
        formatter.visit(new Text("<pre>"))
        formatter.visit(NewLine.INSTANCE)
        formatter.visit(new Text("</pre>"))
        formatter.toHtml().contains("<pre>&lt;pre>\n&lt;/pre></pre>")
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
        formatter.visit(ForegroundColor.of(TextColor.RED))
        formatter.visit(new Text("456"))
        formatter.visit(ForegroundColor.of(TextColor.GREEN))
        formatter.visit(new Text("789"))
        formatter.visit(ForegroundColor.of(TextColor.DEFAULT))
        formatter.visit(new Text("123"))
        formatter.toHtml().contains("<pre>123<span class='ansi-red'>456</span><span class='ansi-green'>789</span>123</pre>")
    }

    def "formats bright and normal foreground color"() {
        expect:
        formatter.visit(new Text("123"))
        formatter.visit(ForegroundColor.of(TextColor.RED))
        formatter.visit(new Text("456"))
        formatter.visit(ForegroundColor.of(TextColor.BRIGHT_RED))
        formatter.visit(new Text("789"))
        formatter.visit(ForegroundColor.of(TextColor.DEFAULT))
        formatter.visit(new Text("123"))
        formatter.toHtml().contains("<pre>123<span class='ansi-red'>456</span><span class='ansi-bright-red'>789</span>123</pre>")
    }

    def "formats background color"() {
        expect:
        formatter.visit(new Text("123"))
        formatter.visit(BackgroundColor.of(TextColor.RED))
        formatter.visit(new Text("456"))
        formatter.visit(BackgroundColor.of(TextColor.GREEN))
        formatter.visit(new Text("789"))
        formatter.visit(BackgroundColor.of(TextColor.BRIGHT_GREEN))
        formatter.visit(new Text("123"))
        formatter.visit(BackgroundColor.of(TextColor.DEFAULT))
        formatter.visit(new Text("456"))
        formatter.toHtml().contains("<pre>123<span class='ansi-red-bg'>456</span><span class='ansi-green-bg'>789</span><span class='ansi-bright-green-bg'>123</span>456</pre>")
    }

    def "formats foreground color and bold"() {
        expect:
        formatter.visit(ForegroundColor.of(TextColor.RED))
        formatter.visit(new Text("123"))
        formatter.visit(BoldOn.INSTANCE)
        formatter.visit(new Text("456"))
        formatter.visit(ForegroundColor.of(TextColor.GREEN))
        formatter.visit(new Text("789"))
        formatter.visit(ForegroundColor.of(TextColor.BRIGHT_GREEN))
        formatter.visit(new Text("123"))
        formatter.visit(ForegroundColor.of(TextColor.DEFAULT))
        formatter.visit(new Text("456"))
        formatter.visit(BoldOff.INSTANCE)
        formatter.visit(new Text("789"))
        formatter.toHtml().contains("<pre><span class='ansi-red'>123</span><span class='ansi-bold ansi-red'>456</span><span class='ansi-bold ansi-green'>789</span><span class='ansi-bold ansi-bright-green'>123</span><span class='ansi-bold'>456</span>789</pre>")
    }

    def "formats foreground color and background color"() {
        expect:
        formatter.visit(ForegroundColor.of(TextColor.RED))
        formatter.visit(new Text("123"))
        formatter.visit(BackgroundColor.of(TextColor.GREEN))
        formatter.visit(new Text("456"))
        formatter.visit(ForegroundColor.of(TextColor.BRIGHT_BLUE))
        formatter.visit(new Text("789"))
        formatter.visit(BackgroundColor.of(TextColor.BRIGHT_GREEN))
        formatter.visit(new Text("123"))
        formatter.visit(ForegroundColor.of(TextColor.DEFAULT))
        formatter.visit(new Text("456"))
        formatter.visit(BackgroundColor.of(TextColor.DEFAULT))
        formatter.visit(new Text("789"))
        formatter.toHtml().contains("<pre><span class='ansi-red'>123</span><span class='ansi-red ansi-green-bg'>456</span><span class='ansi-bright-blue ansi-green-bg'>789</span><span class='ansi-bright-blue ansi-bright-green-bg'>123</span><span class='ansi-bright-green-bg'>456</span>789</pre>")
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
