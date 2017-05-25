package net.rubygrapefruit.ansi.console

import net.rubygrapefruit.ansi.token.ControlSequence
import net.rubygrapefruit.ansi.token.NewLine
import net.rubygrapefruit.ansi.token.Text
import spock.lang.Specification

class AnsiConsoleTest extends Specification {
    def console = new AnsiConsole()

    def "empty console"() {
        expect:
        console.rows.empty
    }

    def "can append empty lines"() {
        expect:
        console.rows.empty

        console.visit(NewLine.INSTANCE)
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""

        console.visit(NewLine.INSTANCE)
        console.visit(NewLine.INSTANCE)
        console.rows.size() == 3
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""
        console.rows[2].visit(new DiagnosticConsole()).toString() == ""
    }

    def "can append lines of text"() {
        expect:
        console.rows.empty

        console.visit(new Text("123"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123"

        console.visit(new Text(" "))
        console.visit(new Text("456"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123 456"

        console.visit(NewLine.INSTANCE)
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123 456"

        console.visit(NewLine.INSTANCE)
        console.rows.size() == 2
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123 456"
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""

        console.visit(new Text("123"))
        console.visit(NewLine.INSTANCE)
        console.rows.size() == 3
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123 456"
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""
        console.rows[2].visit(new DiagnosticConsole()).toString() == "123"
    }

    def "can append control sequences"() {
        expect:
        console.rows.empty

        console.visit(new Text("123"))
        console.visit(new ControlSequence("1m"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123{escape 1m}"

        console.visit(new Text(" "))
        console.visit(new ControlSequence("A"))
        console.visit(NewLine.INSTANCE)
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123{escape 1m} {escape A}"
    }
}
