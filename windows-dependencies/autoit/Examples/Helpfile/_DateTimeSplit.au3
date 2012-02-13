#include <Date.au3>

Local $MyDate, $MyTime
_DateTimeSplit("2005/01/01 14:30", $MyDate, $MyTime)

For $x = 1 To $MyDate[0]
	MsgBox(0, $x, $MyDate[$x])
Next
For $x = 1 To $MyTime[0]
	MsgBox(0, $x, $MyTime[$x])
Next
