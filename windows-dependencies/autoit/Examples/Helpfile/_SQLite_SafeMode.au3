#include <SQLite.au3>
#include <SQLite.dll.au3>

_SQLite_Startup()
ConsoleWrite("_SQLite_LibVersion=" & _SQLite_LibVersion() & @CRLF)
_SQLite_SafeMode(False)
_SQLite_Exec(-1, "CREATE tblTest (a,b,c);"); No database open, this will crash due to SafeMode = false
_SQLite_Shutdown()
