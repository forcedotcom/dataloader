#include <Date.au3>

; Calculated the number of seconds since EPOCH (1970/01/01 00:00:00)
Local $iDateCalc = _DateDiff('s', "1970/01/01 00:00:00", _NowCalc())
MsgBox(4096, "", "Number of seconds since EPOCH: " & $iDateCalc)

; Calculated the number of Hours this year
$iDateCalc = _DateDiff('h', @YEAR & "/01/01 00:00:00", _NowCalc())
MsgBox(4096, "", "Number of Hours this year: " & $iDateCalc)
