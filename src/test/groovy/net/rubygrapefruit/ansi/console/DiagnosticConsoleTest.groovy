package net.rubygrapefruit.ansi.console

import net.rubygrapefruit.ansi.token.CarriageReturn
import net.rubygrapefruit.ansi.token.CursorBackward
import net.rubygrapefruit.ansi.token.CursorDown
import net.rubygrapefruit.ansi.token.CursorForward
import net.rubygrapefruit.ansi.token.CursorUp
import net.rubygrapefruit.ansi.token.EraseInLine
import net.rubygrapefruit.ansi.token.EraseToBeginningOfLine
import net.rubygrapefruit.ansi.token.EraseToEndOfLine
import net.rubygrapefruit.ansi.token.NewLine
import net.rubygrapefruit.ansi.token.Text
import net.rubygrapefruit.ansi.token.UnrecognizedControlSequence
import spock.lang.Specification

class DiagnosticConsoleTest extends Specification {
    def "formats tokens"() {
        def console = new DiagnosticConsole()

        expect:
        console.toString() == ""

        console.visit(new Text("abc"))
        console.toString() == "abc"

        console.visit(new UnrecognizedControlSequence("1;2m"))
        console.toString() == "abc{escape 1;2m}"

        console.visit(NewLine.INSTANCE)
        console.toString() == "abc{escape 1;2m}\n"

        console.visit(new UnrecognizedControlSequence("A"))
        console.toString() == "abc{escape 1;2m}\n{escape A}"

        console.visit(NewLine.INSTANCE)
        console.toString() == "abc{escape 1;2m}\n{escape A}\n"

        console.visit(new CursorUp(4))
        console.visit(new CursorDown(12))
        console.visit(new CursorForward(1))
        console.visit(new CursorBackward(1))
        console.toString() == "abc{escape 1;2m}\n{escape A}\n{cursor-up 4}{cursor-down 12}{cursor-forward 1}{cursor-backward 1}"

        console.visit(EraseInLine.INSTANCE)
        console.visit(EraseToBeginningOfLine.INSTANCE)
        console.visit(EraseToEndOfLine.INSTANCE)
        console.toString() == "abc{escape 1;2m}\n{escape A}\n{cursor-up 4}{cursor-down 12}{cursor-forward 1}{cursor-backward 1}{erase-in-line}{erase-to-beginning-of-line}{erase-to-end-of-line}"

        console.contents(new DiagnosticConsole()).toString() == console.toString()
    }

    def "normalizes cr-nl sequence"() {
        def console = new DiagnosticConsole()

        expect:
        console.toString() == ""

        console.visit(NewLine.INSTANCE)
        console.toString() == "\n"

        console.visit(CarriageReturn.INSTANCE)
        console.toString() == "\n{cr}"

        console.visit(CarriageReturn.INSTANCE)
        console.toString() == "\n{cr}{cr}"

        console.visit(NewLine.INSTANCE)
        console.toString() == "\n{cr}\n"

        console.visit(new Text("abc"))
        console.toString() == "\n{cr}\nabc"

        console.visit(CarriageReturn.INSTANCE)
        console.toString() == "\n{cr}\nabc{cr}"

        console.visit(new Text("abc"))
        console.toString() == "\n{cr}\nabc{cr}abc"

        console.visit(NewLine.INSTANCE)
        console.toString() == "\n{cr}\nabc{cr}abc\n"

        console.contents(new DiagnosticConsole()).toString() == console.toString()
    }
}
