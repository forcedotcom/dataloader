#include <Date.au3>

Local $iDays = _DateDaysInMonth(@YEAR, @MON)
MsgBox(4096, "Days in Month", "This month has " & String($iDays) & " days in it.")
