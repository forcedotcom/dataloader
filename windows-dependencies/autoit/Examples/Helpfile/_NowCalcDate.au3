#include <Date.au3>

Local $Msg = "Test record"
FileWriteLine("Pgm.log", _NowCalcDate() & " :" & $Msg)
