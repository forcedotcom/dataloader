#include <Array.au3>

Local $avArray = StringSplit("4,2,06,8,12,5", ",")

MsgBox(0, 'Max Index String value', _ArrayMaxIndex($avArray, 0, 1))
MsgBox(0, 'Max Index Numeric value', _ArrayMaxIndex($avArray, 1, 1))
