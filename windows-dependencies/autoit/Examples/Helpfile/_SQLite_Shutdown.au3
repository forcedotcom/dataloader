#include <SQLite.au3>
#include <SQLite.dll.au3>
_SQLite_Startup()
If @error Then
	MsgBox(16, "SQLite Error", "SQLite3.dll Can't be Loaded!")
	Exit -1
EndIf
ConsoleWrite("_SQLite_LibVersion=" & _SQLite_LibVersion() & @CRLF)
_SQLite_Shutdown()
