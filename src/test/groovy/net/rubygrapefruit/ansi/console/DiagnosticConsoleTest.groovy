package net.rubygrapefruit.ansi.console

import net.rubygrapefruit.ansi.token.CarriageReturn
import net.rubygrapefruit.ansi.token.ControlSequence
import net.rubygrapefruit.ansi.token.NewLine
import net.rubygrapefruit.ansi.token.Text
import spock.lang.Specification

class DiagnosticConsoleTest extends Specification {
    def "formats tokens"() {
        def console = new DiagnosticConsole()

        expect:
        console.toString() == ""

        console.visit(new Text("abc"))
        console.toString() == "abc"

        console.visit(new ControlSequence("1;2m"))
        console.toString() == "abc{escape 1;2m}"

        console.visit(NewLine.INSTANCE)
        console.toString() == "abc{escape 1;2m}\n"

        console.visit(new ControlSequence("A"))
        console.toString() == "abc{escape 1;2m}\n{escape A}"

        console.visit(NewLine.INSTANCE)
        console.toString() == "abc{escape 1;2m}\n{escape A}\n"
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
    }
}
