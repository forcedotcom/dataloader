#include <SQLite.au3>
#include <SQLite.dll.au3>

Local $aResult, $iRows, $iColumns, $iRval

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

;Example Table
; 	Name        | Age
; 	-----------------------
; 	Alice       | 43
; 	Bob         | 28
; 	Cindy       | 21

If Not _SQLite_Exec(-1, "CREATE TEMP TABLE persons (Name, Age);") = $SQLITE_OK Then _
		MsgBox(16, "SQLite Error", _SQLite_ErrMsg())
If Not _SQLite_Exec(-1, "INSERT INTO persons VALUES ('Alice','43');") = $SQLITE_OK Then _
		MsgBox(16, "SQLite Error", _SQLite_ErrMsg())
If Not _SQLite_Exec(-1, "INSERT INTO persons VALUES ('Bob','28');") = $SQLITE_OK Then _
		MsgBox(16, "SQLite Error", _SQLite_ErrMsg())
If Not _SQLite_Exec(-1, "INSERT INTO persons VALUES ('Cindy','21');") = $SQLITE_OK Then _
		MsgBox(16, "SQLite Error", _SQLite_ErrMsg())

; Query
$iRval = _SQLite_GetTable2d(-1, "SELECT * FROM persons;", $aResult, $iRows, $iColumns)
If $iRval = $SQLITE_OK Then
	_SQLite_Display2DResult($aResult)

;~ 	  $aResult looks like this:
;~
;~ 	 Name   Age
;~ 	 Alice  43
;~ 	 Bob    28
;~ 	 Cindy  21
;~
;~    If the dimensions would be switched in _SQLite_GetTable2d the result would look like this:
;~
;~ 	 Name  Alice  Bob  Cindy
;~ 	 Age   43     28   21

Else
	MsgBox(16, "SQLite Error: " & $iRval, _SQLite_ErrMsg())
EndIf

_SQLite_Close()
_SQLite_Shutdown()
