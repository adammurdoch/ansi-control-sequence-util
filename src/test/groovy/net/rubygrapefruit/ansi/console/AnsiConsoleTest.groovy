package net.rubygrapefruit.ansi.console

import net.rubygrapefruit.ansi.token.*
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

        console.visit(CarriageReturn.INSTANCE)
        console.visit(NewLine.INSTANCE)
        console.rows.size() == 4
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""
        console.rows[2].visit(new DiagnosticConsole()).toString() == ""
        console.rows[3].visit(new DiagnosticConsole()).toString() == ""
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

    def "ignores control sequences (for now)"() {
        expect:
        console.rows.empty

        console.visit(new Text("123"))
        console.visit(new UnrecognizedControlSequence("1m"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123"

        console.visit(new Text(" "))
        console.visit(new CursorDown(12))
        console.visit(NewLine.INSTANCE)
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123 "
    }

    def "overwrites text after cr"() {
        expect:
        console.rows.empty

        console.visit(new Text("123"))
        console.visit(new Text("456"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123456"

        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("ab"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "ab3456"

        console.visit(new Text("cd"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "abcd56"

        console.visit(new Text("efgh"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "abcdefgh"

        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("---"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "---defgh"

        console.visit(CarriageReturn.INSTANCE)
        console.visit(NewLine.INSTANCE)
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "---defgh"

        console.visit(new Text("12"))
        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("4567"))
        console.rows.size() == 2
        console.rows[0].visit(new DiagnosticConsole()).toString() == "---defgh"
        console.rows[1].visit(new DiagnosticConsole()).toString() == "4567"
    }
}
