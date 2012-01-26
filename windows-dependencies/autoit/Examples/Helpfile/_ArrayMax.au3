#include <Array.au3>

Local $avArray = StringSplit("4,2,06,8,12,5", ",")

MsgBox(0, 'Max String value', _ArrayMax($avArray, 0, 1))
MsgBox(0, 'Max Numeric value', _ArrayMax($avArray, 1, 1))
