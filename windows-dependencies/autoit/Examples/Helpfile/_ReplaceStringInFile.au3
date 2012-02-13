#include <File.au3>

Local $find = "BEFORE"
Local $replace = "AFTER"

Local $filename = "C:\_ReplaceStringInFile.test"

Local $msg = "Hello Test " & $find & " Hello Test" & @CRLF
$msg &= "Hello Test" & @CRLF
$msg &= @CRLF
$msg &= $find

FileWrite($filename, $msg)

MsgBox(0, "BEFORE", $msg)

Local $retval = _ReplaceStringInFile($filename, $find, $replace)
If $retval = -1 Then
	MsgBox(0, "ERROR", "The pattern could not be replaced in file: " & $filename & " Error: " & @error)
	Exit
Else
	MsgBox(0, "INFO", "Found " & $retval & " occurances of the pattern: " & $find & " in the file: " & $filename)
EndIf

$msg = FileRead($filename, 1000)
MsgBox(0, "AFTER", $msg)
FileDelete($filename)
