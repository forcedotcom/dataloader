#include <WinAPI.au3>

_Main()

Func _Main()
	Local $win = _WinAPI_GetDesktopWindow()
	MsgBox(0, "", WinGetTitle($win))
	MsgBox(0, "", $win)
EndFunc   ;==>_Main
