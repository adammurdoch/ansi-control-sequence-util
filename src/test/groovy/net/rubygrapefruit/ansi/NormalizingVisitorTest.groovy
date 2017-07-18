package net.rubygrapefruit.ansi

import net.rubygrapefruit.ansi.console.DiagnosticConsole
import net.rubygrapefruit.ansi.token.*
import spock.lang.Specification

class NormalizingVisitorTest extends Specification {
    def target = new DiagnosticConsole()
    def visitor = NormalizingVisitor.of(target)

    def "defers sending bold text change until required"() {
        expect:
        visitor.visit(BoldOn.INSTANCE)
        target.toString() == ""

        visitor.visit(NewLine.INSTANCE)
        target.toString() == "{bold-on}\n"

        visitor.visit(new Text("123"))
        target.toString() == "{bold-on}\n123"

        visitor.visit(BoldOff.INSTANCE)
        target.toString() == "{bold-on}\n123"

        visitor.visit(new Text("456"))
        target.toString() == "{bold-on}\n123{bold-off}456"
    }

    def "does not forward bold text change for span that contains no text"() {
        expect:
        visitor.visit(BoldOn.INSTANCE)
        visitor.visit(BoldOff.INSTANCE)
        target.toString() == ""

        visitor.visit(NewLine.INSTANCE)
        target.toString() == "\n"

        visitor.visit(new Text("123"))
        target.toString() == "\n123"

        visitor.visit(BoldOn.INSTANCE)
        visitor.visit(BoldOff.INSTANCE)
        visitor.visit(BoldOn.INSTANCE)
        visitor.visit(new Text("456"))

        target.toString() == "\n123{bold-on}456"
    }

    def "does not forward duplicate bold text change"() {
        expect:
        visitor.visit(BoldOn.INSTANCE)
        visitor.visit(BoldOn.INSTANCE)
        target.toString() == ""

        visitor.visit(NewLine.INSTANCE)
        target.toString() == "{bold-on}\n"

        visitor.visit(BoldOn.INSTANCE)
        visitor.visit(new Text("123"))
        target.toString() == "{bold-on}\n123"

        visitor.visit(BoldOn.INSTANCE)
        visitor.visit(new CursorBackward(3))
        visitor.visit(BoldOff.INSTANCE)

        target.toString() == "{bold-on}\n123{cursor-backward 3}"
    }

    def "defers sending foreground color change until required"() {
        expect:
        visitor.visit(new ForegroundColor(TextColor.RED))
        target.toString() == ""

        visitor.visit(NewLine.INSTANCE)
        target.toString() == "{foreground-color red}\n"

        visitor.visit(new Text("123"))
        target.toString() == "{foreground-color red}\n123"

        visitor.visit(new ForegroundColor(TextColor.DEFAULT))
        target.toString() == "{foreground-color red}\n123"

        visitor.visit(new Text("456"))
        target.toString() == "{foreground-color red}\n123{foreground-color null}456"
    }

    def "does not forward foreground color change for span that contains no text"() {
        expect:
        visitor.visit(new ForegroundColor(TextColor.RED))
        visitor.visit(new ForegroundColor(TextColor.DEFAULT))
        target.toString() == ""

        visitor.visit(NewLine.INSTANCE)
        target.toString() == "\n"

        visitor.visit(new Text("123"))
        target.toString() == "\n123"

        visitor.visit(new ForegroundColor(TextColor.GREEN))
        visitor.visit(new ForegroundColor(null))
        visitor.visit(new ForegroundColor(TextColor.RED))
        visitor.visit(new Text("456"))

        target.toString() == "\n123{foreground-color red}456"
    }

    def "does not forward duplicate foreground color change"() {
        expect:
        visitor.visit(new ForegroundColor(TextColor.RED))
        visitor.visit(new ForegroundColor(TextColor.RED))
        target.toString() == ""

        visitor.visit(NewLine.INSTANCE)
        target.toString() == "{foreground-color red}\n"

        visitor.visit(new ForegroundColor(TextColor.RED))
        visitor.visit(new Text("123"))
        target.toString() == "{foreground-color red}\n123"

        visitor.visit(new ForegroundColor(TextColor.RED))
        visitor.visit(new CursorBackward(3))
        visitor.visit(new ForegroundColor(null))

        target.toString() == "{foreground-color red}\n123{cursor-backward 3}"
    }

    def "resets text attributes on end"() {
        expect:
        visitor.visit(BoldOn.INSTANCE)
        visitor.visit(new ForegroundColor(TextColor.RED))
        visitor.visit(new Text("123"))
        visitor.endStream()

        target.toString() == "{bold-on}{foreground-color red}123{bold-off}{foreground-color null}"
    }

    def "does not reset on end for deferred changes"() {
        expect:
        visitor.visit(BoldOn.INSTANCE)
        visitor.visit(new ForegroundColor(TextColor.RED))
        visitor.endStream()

        target.toString() == ""
    }
}
