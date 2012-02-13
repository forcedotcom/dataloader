#include <Array.au3>

Local $avArray = StringSplit("4,2,06,8,12,5", ",")

MsgBox(0, 'Min Index String value', _ArrayMinIndex($avArray, 0, 1))
MsgBox(0, 'Min Index Numeric value', _ArrayMinIndex($avArray, 1, 1))
