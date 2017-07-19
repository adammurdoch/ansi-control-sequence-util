A library of utilities for parsing and interpreting text that contains ANSI control sequences.

This can be used for implementing a terminal emulator or for testing or documenting command-line applications.

## TODO

- Handle all cursor movement control sequences:
    - Cursor next line
    - Cursor previous line
    - Cursor horizontal absolute
    - Cursor position
    - Scroll up
    - Scroll down
    - Save cursor position
    - Restore cursor position
- Handle all erase control sequences:
    - Erase part of screen
- Handle text attribute control sequences:
    - Handle background color
    - Handle 256 and 24-bit colors
    - Handle underline
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
- `HtmlFormatter` improvements:
    - Background color should be used for remainder of row when new-line is emitted?
    - Should stream to an `OutputStream`.
    - Escape text content.
- `AnsiConsole` improvements:
    - Erase to start of line and end of line should erase character under cursor
    - Support background color: erase should fill with background color?
    - When replacing tail of span with another span, check whether next span has target attributes already
    - When overwriting span contents at offset 0 of span, maybe merge with previous
    - When erasing bold span that is adjacent to non-bold span
- `DiagnosticConsole` improvements:
    - `contents()` should split lines.
- Replace usages of private `ForegroundColor` constructor from tests.
- Collect text attribute and color diagnostics into their respective classes.
- Add a strongly typed visitor that accepts only text tokens
- Add a strongly typed visitor that accepts only text and text attribute tokens

CSS: `color: rgb(0,0,255);` or `color: #00ff00;` or `color: red;`
CSS: `font-family: monospace`
