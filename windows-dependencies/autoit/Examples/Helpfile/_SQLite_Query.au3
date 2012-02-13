#include <SQLite.au3>
#include <SQLite.dll.au3>

Local $hQuery, $aRow, $sMsg
_SQLite_Startup()
ConsoleWrite("_SQLite_LibVersion=" & _SQLite_LibVersion() & @CRLF)
_SQLite_Open() ; open :memory: Database
_SQLite_Exec(-1, "CREATE TABLE aTest (a,b,c);") ; CREATE a Table
_SQLite_Exec(-1, "INSERT INTO aTest(a,b,c) VALUES ('c','2','World');") ; INSERT Data
_SQLite_Exec(-1, "INSERT INTO aTest(a,b,c) VALUES ('b','3',' ');") ; INSERT Data
_SQLite_Exec(-1, "INSERT INTO aTest(a,b,c) VALUES ('a','1','Hello');") ; INSERT Data
_SQLite_Query(-1, "SELECT c FROM aTest ORDER BY a;", $hQuery) ; the query
While _SQLite_FetchData($hQuery, $aRow) = $SQLITE_OK
	$sMsg &= $aRow[0]
WEnd
_SQLite_Exec(-1, "DROP TABLE aTest;") ; Remove the table
MsgBox(0, "SQLite", "Get Data using a Query : " & $sMsg)
_SQLite_Close()
_SQLite_Shutdown()

;~ Output:
;~
;~ Hello World
