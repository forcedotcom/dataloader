#include <Date.au3>

; Add 5 days to today
Local $sNewDate = _DateAdd('d', 5, _NowCalcDate())
MsgBox(4096, "", "Today + 5 days:" & $sNewDate)

; Subtract 2 weeks from today
$sNewDate = _DateAdd('w', -2, _NowCalcDate())
MsgBox(4096, "", "Today minus 2 weeks: " & $sNewDate)

; Add 15 minutes to current time
$sNewDate = _DateAdd('n', 15, _NowCalc())
MsgBox(4096, "", "Current time +15 minutes: " & $sNewDate)

; Calculated eventlogdate which returns second since 1970/01/01 00:00:00
$sNewDate = _DateAdd('s', 1087497645, "1970/01/01 00:00:00")
MsgBox(4096, "", "Date: " & $sNewDate)
