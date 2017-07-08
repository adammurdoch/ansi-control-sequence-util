A library of utilities for parsing and interpreting text that contains ANSI control sequences.

This can be used for implementing a terminal emulator or for testing or documenting command-line applications.

## TODO

- Handle text attribute control sequences.
    - m or 0m: normal/reset
    - <n>m or <n>;<n>m where <n> is: 
        - 0 reset all attributes
        - 1 bold on
        - 22 bold off
        - 30 black foreground (0,0,0) bright/bold (129, 131, 131)
        - 31 red foreground (194, 54, 33) bold (252,57,31)
        - 32 green foreground (37, 188, 36) bold (49, 231, 34)
        - 33 yellow foreground (173, 173, 39) bold (234, 236, 35)
        - 34 blue foreground (73, 46, 225) bold (88, 51, 255)
        - 35 magenta foreground (211, 56, 211) bold (249, 53, 248)
        - 36 cyan foreground (51, 187, 200) bold (20, 240, 240)
        - 37 white foreground (203, 204, 205) bold (233, 235, 235)
        - 39 default foreground
        - 40 - 47 <x> background
        - 49 default background
        - 90 - 97 <x> bold foreground
        - 100 - 107 <x> bold background
- Render unrecognized control sequences in `AnsiConsole` and `HtmlFormatter` in some highly visible way.
- Escape text content in `HtmlFormatter`.
- `HtmlFormatter` should stream to an `OutputStream`.
- `DiagnosticConsole.contents()` should split lines.
- Add a strongly typed visitor that accepts only text
- Add a strongly typed visitor that accepts only text with attributes

CSS: `color: rgb(0,0,255);` or `color: #00ff00;` or `color: red;`
CSS: `font-family: monospace`
