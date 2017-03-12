# SmallConsole
Small learning project to explore how to implement ANSI console functionality in Java. 
Operating system terminal needs to be in non-blocking character mode instead of default line feed mode to be able to capture and recognize keyboard key strokes. This project currently supports Unix based systems only. Windows command line does not support ANSI codes. Powershell does support ANSI but setting it into non-blocking character mode is a chllenge here which I may try to solve at a later stage.
Basic console functionality includes: recognizing and handling keyboard strokes, allowing command editing with arrow keys, backspace, home/end keys etc. Providing command history with standard navigation using up/down keys.

Sources:
 - https://www.darkcoding.net/software/non-blocking-console-io-is-not-possible/ - it says not possible but it is possible:)
 - http://www.lihaoyi.com/post/BuildyourownCommandLinewithANSIescapecodes.html
 - https://en.wikipedia.org/wiki/ANSI_escape_code
 - http://ascii-table.com/ansi-codes.php
