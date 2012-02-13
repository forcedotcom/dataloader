#include <WinAPI.au3>

_Main()

Func _Main()
	Local $iWord = 11 * 65535
	MsgBox(0, $iWord, "HiWord: " & _WinAPI_HiWord($iWord) & @LF & "LoWord: " & _WinAPI_LoWord($iWord))
EndFunc   ;==>_Main
