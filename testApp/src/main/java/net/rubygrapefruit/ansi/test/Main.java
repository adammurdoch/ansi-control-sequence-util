package net.rubygrapefruit.ansi.test;

import net.rubygrapefruit.ansi.AnsiParser;
import net.rubygrapefruit.ansi.console.AnsiConsole;
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
        if (args.length == 1 && args[0].equals("--display")) {
            display();
        } else {
            parse(args);
        }
    }

    private static void parse(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("USAGE: <html-file> <diagnostic-html-file>");
        }
        Path html = Paths.get(args[0]);
        Path diagnosticHtml = Paths.get(args[1]);

        AnsiConsole mainConsole = new AnsiConsole();
        HtmlFormatter diagnosticConsole = new HtmlFormatter();
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

        try (BufferedWriter writer = Files.newBufferedWriter(diagnosticHtml, Charset.forName("utf-8"))) {
            writer.write(diagnosticConsole.toHtml());
        }
    }

    private static void display() throws IOException {
        Charset utf8 = Charset.forName("utf-8");
        String boldOn = "\u001B[1m";
        String boldOff = "\u001B[22m";
        String cursorDown2 = "\u001B[2B";
        String cursorUp2 = "\u001B[2A";
        String cursorBack20 = "\u001B[20D";
        String cursorBack4 = "\u001B[4D";
        String eraseToEndLine = "\u001B[K";
        String red = "\u001B[0;31m";
        String boldRed = "\u001B[31;1m";
        String reset = "\u001B[0m";
        String defaultColors = "\u001B[39;49m";
        String cursorUp1 = "\u001B[1A";
        String highBlack = "\u001B[0;90m";
        String white = "\u001B[0;37m";
        String blueBackground = "\u001B[44m";
        String brightBlueBackground = "\u001B[104m";
        System.out.write(("\n\n" + cursorUp2 + "normal     " + boldOn + "bold" + boldOff + cursorDown2 + "\r" + red + "red        " + boldOn + "bold red" + cursorBack20 + cursorUp1 + highBlack + "high black " + boldOn + "bold high black\n\n").getBytes(utf8));
        System.out.write((white + blueBackground + "white on blue    " + cursorBack4 + eraseToEndLine + "\n" + brightBlueBackground + "white on bright blue    " + defaultColors + eraseToEndLine + "\n\n").getBytes(utf8));
        System.out.write("\u001B[30mblack    \u001B[1mbold    \u001B[22;90mbright    \u001B[1mbold    \u001B[0;37;40mbackground    \u001B[100mbright    \u001B[0m\n".getBytes(utf8));
        System.out.write("\u001B[31mred      \u001B[1mbold    \u001B[22;91mbright    \u001B[1mbold    \u001B[0;37;41mbackground    \u001B[101mbright    \u001B[0m\n".getBytes(utf8));
        System.out.write("\u001B[32mgreen    \u001B[1mbold    \u001B[22;92mbright    \u001B[1mbold    \u001B[0;37;42mbackground    \u001B[102mbright    \u001B[0m\n".getBytes(utf8));
        System.out.write("\u001B[33myellow   \u001B[1mbold    \u001B[22;93mbright    \u001B[1mbold    \u001B[0;37;43mbackground    \u001B[103mbright    \u001B[0m\n".getBytes(utf8));
        System.out.write("\u001B[34mblue     \u001B[1mbold    \u001B[22;94mbright    \u001B[1mbold    \u001B[0;37;44mbackground    \u001B[104mbright    \u001B[0m\n".getBytes(utf8));
        System.out.write("\u001B[35mmagenta  \u001B[1mbold    \u001B[22;95mbright    \u001B[1mbold    \u001B[0;37;45mbackground    \u001B[105mbright    \u001B[0m\n".getBytes(utf8));
        System.out.write("\u001B[36mcyan     \u001B[1mbold    \u001B[22;96mbright    \u001B[1mbold    \u001B[0;37;46mbackground    \u001B[106mbright    \u001B[0m\n".getBytes(utf8));
        System.out.write("\u001B[37mwhite    \u001B[1mbold    \u001B[22;97mbright    \u001B[1mbold    \u001B[0;30;47mbackground    \u001B[107mbright    \u001B[0m\n".getBytes(utf8));
        System.out.write("\u001B[38;5;160;48;5;250m256      \u001B[1mbold                  \u001B[0m\n".getBytes(utf8));
//        System.out.write("\u001B[38;2;160;45;12mrgb    \u001B[1mbold\u001B[0m\n".getBytes(utf8));
        System.out.write((reset + "\n").getBytes(utf8));
    }
}
