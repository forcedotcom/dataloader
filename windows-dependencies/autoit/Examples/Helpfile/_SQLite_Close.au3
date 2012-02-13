#include <SQLite.au3>
#include <SQLite.dll.au3>

_SQLite_Startup()
If @error Then
	MsgBox(16, "SQLite Error", "SQLite.dll Can't be Loaded!")
	Exit -1
EndIf
ConsoleWrite("_SQLite_LibVersion=" & _SQLite_LibVersion() & @CRLF)
_SQLite_Open() ; Open a :memory: database
If @error Then
	MsgBox(16, "SQLite Error", "Can't Load Database!")
	Exit -1
EndIf
_SQLite_Close()
_SQLite_Shutdown()
