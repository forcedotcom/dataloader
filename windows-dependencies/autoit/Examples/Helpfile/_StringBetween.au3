#include <String.au3>
#include <Array.au3>

_Main()

Func _Main()
	Local $aArray1 = _StringBetween('[18][20][3][5][500][60]', '[', ']')
	_ArrayDisplay($aArray1, 'Default Search')
EndFunc   ;==>_Main
