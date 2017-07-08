package net.rubygrapefruit.ansi.test;

import net.rubygrapefruit.ansi.AnsiParser;
import net.rubygrapefruit.ansi.console.AnsiConsole;
import net.rubygrapefruit.ansi.console.DiagnosticConsole;
import net.rubygrapefruit.ansi.html.HtmlFormatter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("USAGE: <html-file> <diagnostic-html-file>");
        }
        Path html = Paths.get(args[0]);
        Path diagnosticHtml = Paths.get(args[1]);

        AnsiConsole mainConsole = new AnsiConsole();
        DiagnosticConsole diagnosticConsole = new DiagnosticConsole();
        OutputStream outputStream = new AnsiParser().newParser("utf-8", token -> {
            diagnosticConsole.visit(token);
            mainConsole.visit(token);
        });

        byte[] buffer = new byte[1024];
        while (true) {
            int nread = System.in.read(buffer);
            if (nread < 0) {
                break;
            }
            outputStream.write(buffer, 0, nread);
        }

        HtmlFormatter htmlFormatter = new HtmlFormatter();
        mainConsole.contents(htmlFormatter);
        try (BufferedWriter writer = Files.newBufferedWriter(html, Charset.forName("utf-8"))) {
            writer.write(htmlFormatter.toHtml());
        }

        htmlFormatter = new HtmlFormatter();
        diagnosticConsole.contents(htmlFormatter);
        try (BufferedWriter writer = Files.newBufferedWriter(diagnosticHtml, Charset.forName("utf-8"))) {
            writer.write(htmlFormatter.toHtml());
        }
    }
}
