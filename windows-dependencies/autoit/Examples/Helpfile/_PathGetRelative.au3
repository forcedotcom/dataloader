#include <File.au3>


_Main()

Func _Main()
	Local $from, $to, $path
	Local $Wow64 = ""
	If @AutoItX64 Then $Wow64 = "\Wow6432Node"
	Local $sFile = RegRead("HKEY_LOCAL_MACHINE\SOFTWARE" & $Wow64 & "\AutoIt v3\AutoIt", "InstallDir")

	$from = @ScriptDir
	ConsoleWrite("Source Path: " & $from & @CRLF)
	$to = $sFile & "\autoit3.exe"
	ConsoleWrite("Dest Path: " & $to & @CRLF)
	$path = _PathGetRelative($from, $to)
	If @error Then
		ConsoleWrite("Error: " & @error & @CRLF)
		ConsoleWrite("Path: " & $path & @CRLF)
	Else
		ConsoleWrite("Relative Path: " & $path & @CRLF)
		ConsoleWrite("Resolved Path: " & _PathFull($from & "\" & $path) & @CRLF)
	EndIf

EndFunc   ;==>_Main
