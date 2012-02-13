#include <SQLite.au3>
#include <SQLite.dll.au3>

Local $hQuery, $aRow, $iSwitch
_SQLite_Startup()
ConsoleWrite("_SQLite_LibVersion=" & _SQLite_LibVersion() & @CRLF)
_SQLite_Open()
_SQLite_Exec(-1, "CREATE TABLE tblTest (a,b,c);")
_SQLite_Exec(-1, "INSERT INTO tblTest VALUES ('1','1','1');" & _ ; Row 1
		"INSERT INTO tblTest VALUES ('2','2','2');" & _ ; Row 2
		"INSERT INTO tblTest VALUES ('3','3','3');") ; Row 3
_SQLite_Query(-1, "SELECT RowID,* FROM tblTest;", $hQuery)
While _SQLite_FetchData($hQuery, $aRow) = $SQLITE_OK
	$iSwitch = MsgBox(4 + 64, "Row: " & $aRow[0], $aRow[1] & "," & $aRow[2] & "," & $aRow[3] & @LF & _
			"Continue Looping?")
	If $iSwitch = 6 Then ; Yes
		If $aRow[0] = 3 Then _SQLite_QueryReset($hQuery)
	Else ; No
		_SQLite_QueryFinalize($hQuery)
		ExitLoop
	EndIf
WEnd
_SQLite_Close()
_SQLite_Shutdown()
