; Demonstrates the use of StdinWrite()
#include <Constants.au3>

Local $foo = Run("sort.exe", @SystemDir, @SW_HIDE, $STDIN_CHILD + $STDOUT_CHILD)
; Write string to be sorted to child sort.exe's STDIN
StdinWrite($foo, "rat" & @CRLF & "cat" & @CRLF & "bat" & @CRLF)
; Calling with no 2nd arg closes stream
StdinWrite($foo)

; Read from child's STDOUT and show
Local $data
While True
	$data &= StdoutRead($foo)
	If @error Then ExitLoop
	Sleep(25)
WEnd
MsgBox(0, "Debug", $data)
