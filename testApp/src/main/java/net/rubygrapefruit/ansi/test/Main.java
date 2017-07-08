package net.rubygrapefruit.ansi.test;

import net.rubygrapefruit.ansi.AnsiParser;
import net.rubygrapefruit.ansi.console.AnsiConsole;
import net.rubygrapefruit.ansi.console.DiagnosticConsole;
import net.rubygrapefruit.ansi.html.HtmlFormatter;

import java.io.IOException;
import java.io.OutputStream;

public class Main {
    public static void main(String[] args) throws IOException {
        AnsiConsole console = new AnsiConsole();
//        DiagnosticConsole console = new DiagnosticConsole();
        OutputStream outputStream = new AnsiParser().newParser("utf-8", console);

        byte[] buffer = new byte[1024];
        while (true) {
            int nread = System.in.read(buffer);
            if (nread < 0) {
                break;
            }
            outputStream.write(buffer, 0, nread);
        }

        HtmlFormatter htmlFormatter = new HtmlFormatter();
        console.contents(htmlFormatter);
        System.out.print(htmlFormatter.toHtml());
    }
}
