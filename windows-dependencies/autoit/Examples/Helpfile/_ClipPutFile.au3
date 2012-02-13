#include <Misc.au3>
Local $fTest
$fTest = _ClipPutFile(@ScriptFullPath & "|" & @ScriptDir & "|" & @SystemDir)
If Not $fTest Then
	MsgBox(0, "_ClipPutFile() call Failed", "@error = " & @error)
Else
	MsgBox(0, "_ClipPutFile()", "Content of Clipboard:" & @CRLF & ClipGet())
EndIf
