#include <Date.au3>

; Show current date/time in the pc's format
MsgBox(4096, "Pc Long format", _DateTimeFormat(_NowCalc(), 1))
MsgBox(4096, "Pc Short format", _DateTimeFormat(_NowCalc(), 2))
