#include <File.au3>

Local $file = FileOpenDialog("Print File", "", "Text Documents (*.txt)", 1)
If @error Then Exit

Local $print = _FilePrint($file)
If $print Then
	MsgBox(0, "Print", "The file was printed.")
Else
	MsgBox(0, "Print", "Error: " & @error & @CRLF & "The file was not printed.")
EndIf
