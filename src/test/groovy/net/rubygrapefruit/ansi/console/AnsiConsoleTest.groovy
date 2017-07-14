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
        console.contents(new DiagnosticConsole()).toString() == "\n"

        console.visit(NewLine.INSTANCE)
        console.visit(NewLine.INSTANCE)
        console.rows.size() == 4
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""
        console.rows[2].visit(new DiagnosticConsole()).toString() == ""
        console.rows[3].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == "\n\n\n"

        console.visit(CarriageReturn.INSTANCE)
        console.visit(NewLine.INSTANCE)
        console.rows.size() == 5
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""
        console.rows[2].visit(new DiagnosticConsole()).toString() == ""
        console.rows[3].visit(new DiagnosticConsole()).toString() == ""
        console.rows[4].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == "\n\n\n\n"
    }

    def "can append lines of text"() {
        expect:
        console.visit(new Text("123"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123"
        console.contents(new DiagnosticConsole()).toString() == "123"

        console.visit(new Text(" "))
        console.visit(new Text("456"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123 456"
        console.contents(new DiagnosticConsole()).toString() == "123 456"

        console.visit(NewLine.INSTANCE)
        console.rows.size() == 2
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123 456"
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == "123 456\n"

        console.visit(NewLine.INSTANCE)
        console.rows.size() == 3
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123 456"
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""
        console.rows[2].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == "123 456\n\n"

        console.visit(new Text("123"))
        console.visit(NewLine.INSTANCE)
        console.rows.size() == 4
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123 456"
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""
        console.rows[2].visit(new DiagnosticConsole()).toString() == "123"
        console.rows[3].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == "123 456\n\n123\n"
    }

    def "ignores control sequences (for now)"() {
        expect:
        console.visit(new Text("123"))
        console.visit(new UnrecognizedControlSequence("1m"))
        console.visit(new Text("456"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123456"
        console.contents(new DiagnosticConsole()).toString() == "123456"
    }

    def "cr moves cursor to the start of the line and overwrites existing text"() {
        expect:
        console.visit(new Text("123"))
        console.visit(new Text("456"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123456"
        console.contents(new DiagnosticConsole()).toString() == "123456"

        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("ab"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "ab3456"
        console.contents(new DiagnosticConsole()).toString() == "ab3456"

        console.visit(new Text("cd"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "abcd56"
        console.contents(new DiagnosticConsole()).toString() == "abcd56"

        console.visit(new Text("efgh"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "abcdefgh"
        console.contents(new DiagnosticConsole()).toString() == "abcdefgh"

        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("---"))
        console.rows.size() == 1
        console.rows[0].visit(new DiagnosticConsole()).toString() == "---defgh"
        console.contents(new DiagnosticConsole()).toString() == "---defgh"

        console.visit(CarriageReturn.INSTANCE)
        console.visit(NewLine.INSTANCE)
        console.rows.size() == 2
        console.rows[0].visit(new DiagnosticConsole()).toString() == "---defgh"
        console.rows[1].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == "---defgh\n"

        console.visit(new Text("12"))
        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("4567"))
        console.rows.size() == 2
        console.rows[0].visit(new DiagnosticConsole()).toString() == "---defgh"
        console.rows[1].visit(new DiagnosticConsole()).toString() == "4567"
        console.contents(new DiagnosticConsole()).toString() == "---defgh\n4567"
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
        console.contents(new DiagnosticConsole()).toString() == "some text\n\noth___text\nskip\n123"

        // Positioned past the end of line
        console.visit(new CursorUp(1))
        console.visit(new Text("___"))
        console.rows[1].visit(new DiagnosticConsole()).toString() == "      ___"
        console.contents(new DiagnosticConsole()).toString() == "some text\n      ___\noth___text\nskip\n123"

        // Don't move past the first row
        console.visit(new CursorUp(2))
        console.visit(new Text("___"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "some text___"
        console.contents(new DiagnosticConsole()).toString() == "some text___\n      ___\noth___text\nskip\n123"

        // Don't move past the first row
        console.visit(new CursorUp(2))
        console.visit(new Text("+++"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "some text___+++"
        console.contents(new DiagnosticConsole()).toString() == "some text___+++\n      ___\noth___text\nskip\n123"

        console.visit(CarriageReturn.INSTANCE)
        console.visit(NewLine.INSTANCE)
        console.visit(NewLine.INSTANCE)
        console.visit(new Text("+++"))
        console.rows[2].visit(new DiagnosticConsole()).toString() == "+++___text"
        console.contents(new DiagnosticConsole()).toString() == "some text___+++\n      ___\n+++___text\nskip\n123"
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
        console.contents(new DiagnosticConsole()).toString() == "\n\n123"

        // Move beyond last row
        console.visit(new CursorDown(1))
        console.rows.size() == 4
        console.rows[3].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == "\n\n123\n"

        // Cursor positioned beyond end of row
        console.visit(new CursorUp(3))
        console.visit(new CursorDown(1))
        console.visit(new Text("+++"))
        console.rows[1].visit(new DiagnosticConsole()).toString() == "   +++"
        console.contents(new DiagnosticConsole()).toString() == "\n   +++\n123\n"
    }

    def "can move cursor forward"() {
        expect:
        // Cursor positioned beyond end of row
        console.visit(new CursorForward(4))
        console.visit(new Text("123"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "    123"
        console.contents(new DiagnosticConsole()).toString() == "    123"

        console.visit(CarriageReturn.INSTANCE)
        console.visit(new CursorForward(2))
        console.visit(new Text("+++"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "  +++23"
        console.contents(new DiagnosticConsole()).toString() == "  +++23"

        // Move beyond end of next row and back
        console.visit(NewLine.INSTANCE)
        console.visit(new CursorForward(3))
        console.visit(new CursorUp(3))
        console.visit(new Text("---"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "  +---3"
        console.contents(new DiagnosticConsole()).toString() == "  +---3\n"
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
        console.contents(new DiagnosticConsole()).toString() == "some text\n\n1___"

        // Position cursor beyond end of line
        console.visit(new CursorUp(1))
        console.visit(new CursorBackward(2))
        console.visit(new Text("___"))
        console.rows[1].visit(new DiagnosticConsole()).toString() == "  ___"
        console.contents(new DiagnosticConsole()).toString() == "some text\n  ___\n1___"

        // Don't move beyond first column
        console.visit(new CursorBackward(10))
        console.visit(new Text("abc"))
        console.rows[1].visit(new DiagnosticConsole()).toString() == "abc__"
        console.contents(new DiagnosticConsole()).toString() == "some text\nabc__\n1___"

        // Don't move beyond first column
        console.visit(new CursorBackward(3))
        console.visit(new CursorBackward(10))
        console.visit(new Text("++"))
        console.rows[1].visit(new DiagnosticConsole()).toString() == "++c__"
        console.contents(new DiagnosticConsole()).toString() == "some text\n++c__\n1___"

        // Don't move beyond first column
        console.visit(CarriageReturn.INSTANCE)
        console.visit(new CursorBackward(3))
        console.visit(new Text("..."))
        console.rows[1].visit(new DiagnosticConsole()).toString() == "...__"
        console.contents(new DiagnosticConsole()).toString() == "some text\n...__\n1___"
    }

    def "can erase in line"() {
        expect:
        console.visit(EraseInLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == ""

        console.visit(new Text("12345"))
        console.visit(new CursorBackward(2))
        console.visit(EraseInLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "   "
        console.contents(new DiagnosticConsole()).toString() == "   "

        console.visit(new Text("+++"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "   +++"
        console.contents(new DiagnosticConsole()).toString() == "   +++"

        console.visit(new CursorForward(3))
        console.visit(EraseInLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "         "
        console.contents(new DiagnosticConsole()).toString() == "         "

        console.visit(new Text("+++"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "         +++"
        console.contents(new DiagnosticConsole()).toString() == "         +++"

        console.visit(NewLine.INSTANCE)
        console.visit(new CursorUp(1))
        console.visit(EraseInLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == "\n"
    }

    def "can erase to beginning of line"() {
        expect:
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == ""

        console.visit(new Text("12345"))
        console.visit(new CursorBackward(2))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "   45"
        console.contents(new DiagnosticConsole()).toString() == "   45"

        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("+++"))
        console.visit(CarriageReturn.INSTANCE)
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "+++45"
        console.contents(new DiagnosticConsole()).toString() == "+++45"

        console.visit(NewLine.INSTANCE)
        console.visit(new CursorUp(1))
        console.visit(new CursorForward(2))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "  +45"
        console.contents(new DiagnosticConsole()).toString() == "  +45\n"

        console.visit(new CursorForward(3))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "     "
        console.contents(new DiagnosticConsole()).toString() == "     \n"
    }

    def "can erase to end of line"() {
        expect:
        console.visit(EraseToEndOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == ""

        console.visit(new Text("123"))
        console.visit(EraseToEndOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123"
        console.contents(new DiagnosticConsole()).toString() == "123"

        console.visit(CarriageReturn.INSTANCE)
        console.visit(EraseToEndOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == ""

        console.visit(new Text("123"))
        console.visit(new CursorBackward(2))
        console.visit(EraseToEndOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "1"
        console.contents(new DiagnosticConsole()).toString() == "1"

        console.visit(NewLine.INSTANCE)
        console.visit(new CursorUp(1))
        console.visit(EraseToEndOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == "\n"
    }

    def "can apply bold text"() {
        expect:
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(NewLine.INSTANCE)
        console.visit(new Text("+++"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("..."))
        console.visit(NewLine.INSTANCE)
        console.visit(new Text("123"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123{bold-on}456{bold-off}"
        console.rows[1].visit(new DiagnosticConsole()).toString() == "{bold-on}+++{bold-off}..."
        console.rows[2].visit(new DiagnosticConsole()).toString() == "123"
        console.contents(new DiagnosticConsole()).toString() == "123{bold-on}456\n+++{bold-off}...\n123"
    }

    def "can overwrite bold text"() {
        expect:
        console.visit(new Text("123456"))
        console.visit(NewLine.INSTANCE)
        console.visit(new Text("+++"))
        console.visit(new CursorUp(1))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("----"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123{bold-on}----{bold-off}"
        console.rows[1].visit(new DiagnosticConsole()).toString() == "+++"
        console.contents(new DiagnosticConsole()).toString() == "123{bold-on}----\n{bold-off}+++"

        console.visit(new CursorBackward(3))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("--"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123{bold-on}-{bold-off}--{bold-on}-{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "123{bold-on}-{bold-off}--{bold-on}-\n{bold-off}+++"

        console.visit(new CursorBackward(4))
        console.visit(EraseToEndOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "12"
        console.contents(new DiagnosticConsole()).toString() == "12\n+++"

        console.visit(new CursorForward(4))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("..."))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "12    {bold-on}...{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "12    {bold-on}...\n{bold-off}+++"

        console.visit(new CursorForward(2))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("!"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "12    {bold-on}...{bold-off}  !"
        console.contents(new DiagnosticConsole()).toString() == "12    {bold-on}...{bold-off}  !\n+++"
    }

    def "can overwrite bold text with bold text"() {
        expect:
        // Start of span to middle of span
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("1234"))
        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("++"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "{bold-on}++34{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "{bold-on}++34{bold-off}"

        // Middle of span to end
        console.visit(new Text("++"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "{bold-on}++++{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "{bold-on}++++{bold-off}"

        // Start of span to end
        console.visit(new CursorBackward(4))
        console.visit(new Text("1234"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "{bold-on}1234{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "{bold-on}1234{bold-off}"

        // Append from middle of span
        console.visit(new CursorBackward(2))
        console.visit(new Text("...."))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "{bold-on}12....{bold-off}"
    }

    def "can overwrite normal text with bold text"() {
        expect:
        // Start of span to middle of span
        console.visit(new Text("1234"))
        console.visit(CarriageReturn.INSTANCE)
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("++"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "{bold-on}++{bold-off}34"
        console.contents(new DiagnosticConsole()).toString() == "{bold-on}++{bold-off}34"

        // Middle of span to end
        console.visit(BoldOff.INSTANCE)
        console.visit(CarriageReturn.INSTANCE)
        console.visit(EraseToEndOfLine.INSTANCE)
        console.visit(new Text("1234"))
        console.visit(new CursorBackward(2))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("++"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "12{bold-on}++{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "12{bold-on}++{bold-off}"

        // Start of span to end
        console.visit(NewLine.INSTANCE)
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("1234"))
        console.visit(CarriageReturn.INSTANCE)
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("++++"))
        console.rows[1].visit(new DiagnosticConsole()).toString() == "{bold-on}++++{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "12{bold-on}++\n++++{bold-off}"

        // Append from middle of span
        console.visit(CarriageReturn.INSTANCE)
        console.visit(EraseToEndOfLine.INSTANCE)
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("1234"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new CursorBackward(2))
        console.visit(new Text("...."))
        console.rows[1].visit(new DiagnosticConsole()).toString() == "12{bold-on}....{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "12{bold-on}++\n{bold-off}12{bold-on}....{bold-off}"
    }

    def "can overwrite normal text followed by bold text with bold text"() {
        expect:
        // Overwrite whole span
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("789"))
        console.visit(CarriageReturn.INSTANCE)
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("..."))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "{bold-on}...456{bold-off}789"
        console.contents(new DiagnosticConsole()).toString() == "{bold-on}...456{bold-off}789"

        // Overwrite from start to middle of span
        console.visit(CarriageReturn.INSTANCE)
        console.visit(EraseInLine.INSTANCE)
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("789"))
        console.visit(CarriageReturn.INSTANCE)
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text(".."))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "{bold-on}..{bold-off}3{bold-on}456{bold-off}789"
        console.contents(new DiagnosticConsole()).toString() == "{bold-on}..{bold-off}3{bold-on}456{bold-off}789"

        // Overwrite from middle to end of span
        console.visit(CarriageReturn.INSTANCE)
        console.visit(EraseInLine.INSTANCE)
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("789"))
        console.visit(new CursorBackward(8))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text(".."))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "1{bold-on}..456{bold-off}789"
        console.contents(new DiagnosticConsole()).toString() == "1{bold-on}..456{bold-off}789"

        // Overwrite from middle to middle of next span
        console.visit(CarriageReturn.INSTANCE)
        console.visit(EraseInLine.INSTANCE)
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("789"))
        console.visit(new CursorBackward(8))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("...."))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "1{bold-on}....6{bold-off}789"
        console.contents(new DiagnosticConsole()).toString() == "1{bold-on}....6{bold-off}789"

        // Overwrite from start to middle of next span
        console.visit(CarriageReturn.INSTANCE)
        console.visit(EraseInLine.INSTANCE)
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("789"))
        console.visit(CarriageReturn.INSTANCE)
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("....."))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "{bold-on}.....6{bold-off}789"
        console.contents(new DiagnosticConsole()).toString() == "{bold-on}.....6{bold-off}789"

        // Overwrite from middle to end of next span
        console.visit(CarriageReturn.INSTANCE)
        console.visit(EraseInLine.INSTANCE)
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("789"))
        console.visit(new CursorBackward(8))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("....."))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "1{bold-on}.....{bold-off}789"
        console.contents(new DiagnosticConsole()).toString() == "1{bold-on}.....{bold-off}789"

        // Overwrite from middle to middle of last span
        console.visit(CarriageReturn.INSTANCE)
        console.visit(EraseInLine.INSTANCE)
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("789"))
        console.visit(new CursorBackward(8))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("......."))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "1{bold-on}.......{bold-off}9"
        console.contents(new DiagnosticConsole()).toString() == "1{bold-on}.......{bold-off}9"
    }

    def "can write bold text beyond end of line"() {
        expect:
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(new CursorForward(2))
        console.visit(new Text("456"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "{bold-on}123{bold-off}  {bold-on}456{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "{bold-on}123{bold-off}  {bold-on}456{bold-off}"
    }

    def "can overwrite bold text with normal text"() {
        expect:
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("456"))
        console.visit(new CursorBackward(5))
        console.visit(new Text("+++"))
        console.rows[0].visit(new DiagnosticConsole()).toString() == "{bold-on}1{bold-off}+++56"
        console.contents(new DiagnosticConsole()).toString() == "{bold-on}1{bold-off}+++56"
    }

    def "can erase to end of line over text with a mix of bold"() {
        expect:
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("789"))
        console.visit(new CursorBackward(4))
        console.visit(EraseToEndOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123{bold-on}45{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "123{bold-on}45{bold-off}"

        console.visit(BoldOn.INSTANCE)
        console.visit(new CursorForward(2))
        console.visit(EraseToEndOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123{bold-on}45{bold-off}  "
        console.contents(new DiagnosticConsole()).toString() == "123{bold-on}45{bold-off}  "

        console.visit(new Text("+++"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("..."))
        console.visit(new CursorForward(2))
        console.visit(EraseToEndOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123{bold-on}45{bold-off}  {bold-on}+++{bold-off}...  "
        console.contents(new DiagnosticConsole()).toString() == "123{bold-on}45{bold-off}  {bold-on}+++{bold-off}...  "
    }

    def "can erase to start of line over text with a mix of bold"() {
        expect:
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("789"))
        console.visit(new CursorBackward(4))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "     {bold-on}6{bold-off}789"
        console.contents(new DiagnosticConsole()).toString() == "     {bold-on}6{bold-off}789"

        console.visit(NewLine.INSTANCE)
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("456"))
        console.visit(CarriageReturn.INSTANCE)
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[1].visit(new DiagnosticConsole()).toString() == "{bold-on}123{bold-off}456"
        console.contents(new DiagnosticConsole()).toString() == "     {bold-on}6{bold-off}789\n{bold-on}123{bold-off}456"

        console.visit(new CursorForward(1))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[1].visit(new DiagnosticConsole()).toString() == " {bold-on}23{bold-off}456"
        console.contents(new DiagnosticConsole()).toString() == "     {bold-on}6{bold-off}789\n {bold-on}23{bold-off}456"

        console.visit(new CursorForward(2))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[1].visit(new DiagnosticConsole()).toString() == "   456"
        console.contents(new DiagnosticConsole()).toString() == "     {bold-on}6{bold-off}789\n   456"
    }

    def "erase to start of line from leftmost column does nothing"() {
        expect:
        // Empty line
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == ""

        // Default text attributes
        console.visit(new Text("123"))
        console.visit(CarriageReturn.INSTANCE)
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "123"
        console.contents(new DiagnosticConsole()).toString() == "123"

        // Bold span
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("789"))
        console.visit(CarriageReturn.INSTANCE)
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "{bold-on}456{bold-off}789"
        console.contents(new DiagnosticConsole()).toString() == "{bold-on}456{bold-off}789"
    }

    def "erase to start of line from inside leftmost span with default text attributes"() {
        expect:
        // Inside span
        console.visit(new Text("123"))
        console.visit(new CursorBackward(2))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == " 23"
        console.contents(new DiagnosticConsole()).toString() == " 23"

        // End of span
        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("1"))
        console.visit(new CursorForward(2))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "   "
        console.contents(new DiagnosticConsole()).toString() == "   "

        // Inside span with following content
        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("1234"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("567"))
        console.visit(new CursorBackward(5))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "  34{bold-on}567{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "  34{bold-on}567{bold-off}"

        // End of span with following content
        console.visit(new CursorForward(2))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "    {bold-on}567{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "    {bold-on}567{bold-off}"
    }

    def "erase to start of line from inside leftmost span with bold text"() {
        expect:
        // Inside span
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(new CursorBackward(2))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == " {bold-on}23{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == " {bold-on}23{bold-off}"

        // End of span
        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("1"))
        console.visit(new CursorForward(2))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "   "
        console.contents(new DiagnosticConsole()).toString() == "   "

        // Inside span with following content
        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("1234"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("567"))
        console.visit(new CursorBackward(5))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "  {bold-on}34{bold-off}567"
        console.contents(new DiagnosticConsole()).toString() == "  {bold-on}34{bold-off}567"

        // End of span with following content
        console.visit(NewLine.INSTANCE)
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("456"))
        console.visit(new CursorBackward(3))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[1].visit(new DiagnosticConsole()).toString() == "   456"
        console.contents(new DiagnosticConsole()).toString() == "  {bold-on}34{bold-off}567\n   456"
    }

    def "erase to start of line from outside leftmost span with default text attributes"() {
        expect:
        // Inside next span
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(new CursorBackward(2))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "    {bold-on}56{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "    {bold-on}56{bold-off}"

        // End of next span
        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "      "
        console.contents(new DiagnosticConsole()).toString() == "      "

        // Inside next span with following content
        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("789"))
        console.visit(new CursorBackward(4))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "     {bold-on}6{bold-off}789"
        console.contents(new DiagnosticConsole()).toString() == "     {bold-on}6{bold-off}789"

        // End of next span with following content
        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("789"))
        console.visit(new CursorBackward(3))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "      789"
        console.contents(new DiagnosticConsole()).toString() == "      789"
    }

    def "erase to start of line from outside leftmost span with bold text"() {
        expect:
        // Inside next span
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("456"))
        console.visit(new CursorBackward(2))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "    56"
        console.contents(new DiagnosticConsole()).toString() == "    56"

        // End of next span
        console.visit(CarriageReturn.INSTANCE)
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("456"))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "      "
        console.contents(new DiagnosticConsole()).toString() == "      "

        // Inside next span with following content
        console.visit(CarriageReturn.INSTANCE)
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("456"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("789"))
        console.visit(new CursorBackward(4))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "     6{bold-on}789{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "     6{bold-on}789{bold-off}"

        // End of next span with following content
        console.visit(CarriageReturn.INSTANCE)
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("456"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("789"))
        console.visit(new CursorBackward(3))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "      {bold-on}789{bold-off}"
        console.contents(new DiagnosticConsole()).toString() == "      {bold-on}789{bold-off}"
    }

    def "erase to start of line from beyond end of line"() {
        expect:
        // Single span normal attributes
        console.visit(new Text("123"))
        console.visit(new CursorForward(3))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "      "
        console.contents(new DiagnosticConsole()).toString() == "      "

        // Single span bold text
        console.visit(CarriageReturn.INSTANCE)
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(new CursorForward(3))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "      "
        console.contents(new DiagnosticConsole()).toString() == "      "

        // Normal span followed multiple spans
        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(new CursorForward(3))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "         "
        console.contents(new DiagnosticConsole()).toString() == "         "

        // Bold span followed multiple spans
        console.visit(CarriageReturn.INSTANCE)
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("456"))
        console.visit(new CursorForward(3))
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "         "
        console.contents(new DiagnosticConsole()).toString() == "         "
    }

    def "can erase line containing text with a mix of bold"() {
        expect:
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(CarriageReturn.INSTANCE)
        console.visit(EraseInLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == ""
        console.contents(new DiagnosticConsole()).toString() == ""

        console.visit(new Text("123"))
        console.visit(BoldOff.INSTANCE)
        console.visit(new Text("456"))
        console.visit(EraseInLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "      "
        console.contents(new DiagnosticConsole()).toString() == "      "

        console.visit(CarriageReturn.INSTANCE)
        console.visit(new Text("123"))
        console.visit(BoldOn.INSTANCE)
        console.visit(new Text("456"))
        console.visit(new CursorForward(2))
        console.visit(EraseInLine.INSTANCE)
        console.rows[0].visit(new DiagnosticConsole()).toString() == "        "
        console.contents(new DiagnosticConsole()).toString() == "        "
    }
}
