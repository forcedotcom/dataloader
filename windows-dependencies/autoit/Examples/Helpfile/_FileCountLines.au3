#include <File.au3>

Local $CountLines = _FileCountLines("error.log")
MsgBox(64, "Error log recordcount", "There are " & $CountLines & " in the error.log.")
Exit

