#include <SQLite.au3>
#include <SQLite.dll.au3>
#include <File.au3>

_SQLite_Startup()
If @error Then
	MsgBox(16, "SQLite Error", "SQLite3.dll Can't be Loaded!")
	Exit -1
EndIf
ConsoleWrite("_SQLite_LibVersion=" & _SQLite_LibVersion() & @CRLF)

_SQLite_Open() ; Creates a :memory: database and don't use its handle to refer to it
If @error Then
	MsgBox(16, "SQLite Error", "Can't create a memory Database!")
	Exit -1
EndIf
_SQLite_Close()

Local $hMemDb = _SQLite_Open() ; Creates a :memory: database
If @error Then
	MsgBox(16, "SQLite Error", "Can't create a memory Database!")
	Exit -1
EndIf

Local $hTmpDb = _SQLite_Open('') ; Creates a temporary disk database
If @error Then
	MsgBox(16, "SQLite Error", "Can't create a temporary Database!")
	Exit -1
EndIf

Local $sDbName = _TempFile()
Local $hDskDb = _SQLite_Open($sDbName) ; Open a permanent disk database
If @error Then
	MsgBox(16, "SQLite Error", "Can't open or create a permanent Database!")
	Exit -1
EndIf

; we can use the 3 database as needed by refering to their handle

; close the Dbs we created, in any order
_SQLite_Close($hTmpDb) ; temporary database are deleted automatically at Close
_SQLite_Close($hDskDb) ; DB is a regular file that could be reopened later
_SQLite_Close($hMemDb)

; we don't really need that DB
FileDelete($sDbName)

_SQLite_Shutdown()
