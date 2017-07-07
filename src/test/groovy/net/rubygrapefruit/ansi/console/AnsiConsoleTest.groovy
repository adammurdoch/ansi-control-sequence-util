package net.rubygrapefruit.ansi.console

import net.rubygrapefruit.ansi.token.*
import spock.lang.Specification

class AnsiConsoleTest extends Specification {
    def console = new AnsiConsole()

    def "empty console"() {
        expect:
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
    }

    def "can append empty lines"() {
        expect:
        console.visit(NewLine.INSTANCE)
        console.rows.size() == 2
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""

        console.visit(NewLine.INSTANCE)
        console.visit(NewLine.INSTANCE)
        console.rows.size() == 4
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""
        console.rows[2].visit(new DiagnosticConsole()).toString() == ""
        console.rows[3].visit(new DiagnosticConsole()).toString() == ""

        console.visit(CarriageReturn.INSTANCE)
        console.visit(NewLine.INSTANCE)
        console.rows.size() == 5
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""
        console.rows[2].visit(new DiagnosticConsole()).toString() == ""
        console.rows[3].visit(new DiagnosticConsole()).toString() == ""
        console.rows[4].visit(new DiagnosticConsole()).toString() == ""
    }

    def "can append lines of text"() {
        expect:
        console.visit(new Text("123"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123"

        console.visit(new Text(" "))
        console.visit(new Text("456"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123 456"

        console.visit(NewLine.INSTANCE)
        console.rows.size() == 2
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123 456"
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""

        console.visit(NewLine.INSTANCE)
        console.rows.size() == 3
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123 456"
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""
        console.rows[2].visit(new DiagnosticConsole()).toString() == ""

        console.visit(new Text("123"))
        console.visit(NewLine.INSTANCE)
        console.rows.size() == 4
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123 456"
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""
        console.rows[2].visit(new DiagnosticConsole()).toString() == "123"
        console.rows[3].visit(new DiagnosticConsole()).toString() == ""
    }

    def "ignores control sequences (for now)"() {
        expect:
        console.visit(new Text("123"))
        console.visit(new UnrecognizedControlSequence("1m"))
        console.visit(new Text("456"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123456"
    }

    def "cr moves cursor to the start of the line and overwrites existing text"() {
        expect:
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
        console.rows.size() == 2
        console.rows[0].visit(new DiagnosticConsole()).toString() == "---defgh"
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""

        console.visit(new Text("12"))
        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("4567"))
        console.rows.size() == 2
        console.rows[0].visit(new DiagnosticConsole()).toString() == "---defgh"
        console.rows[1].visit(new DiagnosticConsole()).toString() == "4567"
    }

    def "can move cursor up"() {
        given:
        console.visit(new Text("some text"))
        console.visit(NewLine.INSTANCE)
        console.visit(NewLine.INSTANCE)
        console.visit(new Text("other text"))
        console.visit(NewLine.INSTANCE)
        console.visit(new Text("skip"))
        console.visit(NewLine.INSTANCE)
        console.visit(new Text("123"))

        expect:
        console.visit(new CursorUp(2))
        console.visit(new Text("___"))
        console.rows[2].visit(new DiagnosticConsole()).toString() == "oth___text"

        // Positioned past the end of line
        console.visit(new CursorUp(1))
        console.visit(new Text("___"))
        console.rows[1].visit(new DiagnosticConsole()).toString() == "      ___"

        // Don't move past the first row
        console.visit(new CursorUp(2))
        console.visit(new Text("___"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "some text___"

        // Don't move past the first row
        console.visit(new CursorUp(2))
        console.visit(new Text("+++"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "some text___+++"

        console.visit(CarriageReturn.INSTANCE)
        console.visit(NewLine.INSTANCE)
        console.visit(NewLine.INSTANCE)
        console.visit(new Text("+++"))
        console.rows[2].visit(new DiagnosticConsole()).toString() == "+++___text"
    }

    def "can move cursor down"() {
        expect:
        // Move beyond last row
        console.visit(new CursorDown(2))
        console.rows.size() == 3
        console.visit(new Text("123"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""
        console.rows[2].visit(new DiagnosticConsole()).toString() == "123"

        // Move beyond last row
        console.visit(new CursorDown(1))
        console.rows.size() == 4
        console.rows[3].visit(new DiagnosticConsole()).toString() == ""

        // Cursor positioned beyond end of row
        console.visit(new CursorUp(3))
        console.visit(new CursorDown(1))
        console.visit(new Text("+++"))
        console.rows[1].visit(new DiagnosticConsole()).toString() == "   +++"
    }

    def "can move cursor forward"() {
        expect:
        // Cursor positioned beyond end of row
        console.visit(new CursorForward(4))
        console.visit(new Text("123"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "    123"

        console.visit(CarriageReturn.INSTANCE)
        console.visit(new CursorForward(2))
        console.visit(new Text("+++"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "  +++23"

        // Move beyond end of next row and back
        console.visit(NewLine.INSTANCE)
        console.visit(new CursorForward(3))
        console.visit(new CursorUp(3))
        console.visit(new Text("---"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "  +---3"
    }

    def "can move cursor backward"() {
        given:
        console.visit(new Text("some text"))
        console.visit(NewLine.INSTANCE)
        console.visit(NewLine.INSTANCE)
        console.visit(new Text("123"))

        expect:
        console.visit(new CursorBackward(2))
        console.visit(new Text("___"))
        console.rows[2].visit(new DiagnosticConsole()).toString() == "1___"

        // Position cursor beyond end of line
        console.visit(new CursorUp(1))
        console.visit(new CursorBackward(2))
        console.visit(new Text("___"))
        console.rows[1].visit(new DiagnosticConsole()).toString() == "  ___"

        // Don't move beyond first column
        console.visit(new CursorBackward(10))
        console.visit(new Text("abc"))
        console.rows[1].visit(new DiagnosticConsole()).toString() == "abc__"

        // Don't move beyond first column
        console.visit(new CursorBackward(3))
        console.visit(new CursorBackward(10))
        console.visit(new Text("++"))
        console.rows[1].visit(new DiagnosticConsole()).toString() == "++c__"

        // Don't move beyond first column
        console.visit(CarriageReturn.INSTANCE)
        console.visit(new CursorBackward(3))
        console.visit(new Text("..."))
        console.rows[1].visit(new DiagnosticConsole()).toString() == "...__"
    }
}
