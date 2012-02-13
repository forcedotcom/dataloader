#include <SQLite.au3>
#include <SQLite.dll.au3>

_SQLite_Startup()
ConsoleWrite("_SQLite_LibVersion=" & _SQLite_LibVersion() & @CRLF)
Local $sTestString, $i, $aRow
For $i = 1 To 255
	$sTestString &= Chr($i)
Next
_SQLite_Open()
_SQLite_Exec(-1, "CREATE TABLE test (a)")
_SQLite_Exec(-1, "INSERT INTO test VALUES (" & _SQLite_Escape($sTestString) & ")")
_SQLite_QuerySingleRow(-1, "SELECT a FROM test LIMIT 1", $aRow)
If $aRow[0] = $sTestString Then ConsoleWrite("! identical !" & @CRLF)
_SQLite_Shutdown()
