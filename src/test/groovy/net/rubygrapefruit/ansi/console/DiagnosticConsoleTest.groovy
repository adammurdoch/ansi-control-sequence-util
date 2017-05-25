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

        console.visit(new NewLine())
        console.toString() == "abc{escape 1;2m}\n"

        console.visit(new ControlSequence("A"))
        console.toString() == "abc{escape 1;2m}\n{escape A}"

        console.visit(new NewLine())
        console.toString() == "abc{escape 1;2m}\n{escape A}\n"
    }

    def "normalizes cr-nl sequence"() {
        def console = new DiagnosticConsole()

        expect:
        console.toString() == ""

        console.visit(new NewLine())
        console.toString() == "\n"

        console.visit(new CarriageReturn())
        console.toString() == "\n\r"

        console.visit(new CarriageReturn())
        console.toString() == "\n\r\r"

        console.visit(new NewLine())
        console.toString() == "\n\r\n"

        console.visit(new Text("abc"))
        console.toString() == "\n\r\nabc"

        console.visit(new CarriageReturn())
        console.toString() == "\n\r\nabc\r"

        console.visit(new Text("abc"))
        console.toString() == "\n\r\nabc\rabc"

        console.visit(new NewLine())
        console.toString() == "\n\r\nabc\rabc\n"
    }
}
