#include<String.au3>
; Inserts three "moving" underscores and prints them to the console
For $i_loop = -20 To 20
	ConsoleWrite($i_loop & @TAB & _StringInsert("Supercalifragilistic", "___", $i_loop) & @CRLF)
Next
