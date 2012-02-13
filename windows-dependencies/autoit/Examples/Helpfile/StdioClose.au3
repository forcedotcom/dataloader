; Demonstrates StdioClose()
#include <Constants.au3>

Local $pid = Run(@ComSpec & " /c dir foo.bar", @SystemDir, @SW_HIDE, $STDERR_MERGED + $STDOUT_CHILD)
StdioClose($pid)

; No data will be read because we closed all the streams we would read from.
Local $line
While 1
	$line = StdoutRead($pid)
	If @error Then ExitLoop
	MsgBox(0, "STDOUT read:", $line)
WEnd

While 1
	$line = StderrRead($pid)
	If @error Then ExitLoop
	MsgBox(0, "STDERR read:", $line)
WEnd

MsgBox(0, "Debug", "Exiting...")
