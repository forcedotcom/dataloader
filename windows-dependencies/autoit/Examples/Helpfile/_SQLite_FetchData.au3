#include <SQLite.au3>
#include <SQLite.dll.au3>

Local $hQuery, $aRow, $aNames
_SQLite_Startup()
ConsoleWrite("_SQLite_LibVersion=" & _SQLite_LibVersion() & @CRLF)
_SQLite_Open() ; open :memory: Database
_SQLite_Exec(-1, "CREATE TABLE aTest (A,B,C);")
_SQLite_Exec(-1, "INSERT INTO aTest(a,b,c) VALUES ('c','2','World');")
_SQLite_Exec(-1, "INSERT INTO aTest(a,b,c) VALUES ('b','3',' ');")
_SQLite_Exec(-1, "INSERT INTO aTest(a,b,c) VALUES ('a','1','Hello');")
_SQLite_Query(-1, "SELECT ROWID,* FROM aTest ORDER BY a;", $hQuery)
_SQLite_FetchNames($hQuery, $aNames)
ConsoleWrite(StringFormat(" %-10s  %-10s  %-10s  %-10s ", $aNames[0], $aNames[1], $aNames[2], $aNames[3]) & @CRLF)
While _SQLite_FetchData($hQuery, $aRow, False, False) = $SQLITE_OK ; Read Out the next Row
	ConsoleWrite(StringFormat(" %-10s  %-10s  %-10s  %-10s ", $aRow[0], $aRow[1], $aRow[2], $aRow[3]) & @CRLF)
WEnd
_SQLite_QueryFinalize($hQuery)
_SQLite_Exec(-1, "DROP TABLE aTest;")
_SQLite_Close()
_SQLite_Shutdown()

;~ Output:
;~
;~  rowid       A           B           C
;~  3           a           1           Hello
;~  2           b           3
;~  1           c           2           World
