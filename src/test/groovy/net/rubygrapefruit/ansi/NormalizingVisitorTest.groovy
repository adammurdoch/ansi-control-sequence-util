package net.rubygrapefruit.ansi

import net.rubygrapefruit.ansi.console.DiagnosticConsole
import net.rubygrapefruit.ansi.token.BoldOff
import net.rubygrapefruit.ansi.token.BoldOn
import net.rubygrapefruit.ansi.token.CursorBackward
import net.rubygrapefruit.ansi.token.NewLine
import net.rubygrapefruit.ansi.token.Text
import spock.lang.Specification

class NormalizingVisitorTest extends Specification {
    def target = new DiagnosticConsole()
    def visitor = new NormalizingVisitor(target)

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

    def "resets text attributes on end"() {
        expect:
        visitor.visit(BoldOn.INSTANCE)
        visitor.visit(new Text("123"))
        visitor.endStream()

        target.toString() == "{bold-on}123{bold-off}"
    }
}
