#include <Date.au3>

If _DateIsLeapYear(@YEAR) Then
	MsgBox(4096, "Leap Year", "This year is a leap year.")
Else
	MsgBox(4096, "Leap Year", "This year is not a leap year.")
EndIf
