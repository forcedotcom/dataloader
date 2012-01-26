#include <File.au3>

; Example 1
Local $hFile = FileOpen(@ScriptDir & "\Example.log", 1) ; Open the logfile in write mode.

_FileWriteLog($hFile, "Text 1") ; Write to the logfile passing the filehandle returned by FileOpen.
FileClose($hFile) ; Close the filehandle to release the file.

; Example 2
_FileWriteLog(@ScriptDir & "\Example.log", "Text 2") ; Write to the logfile passing the filepath.
