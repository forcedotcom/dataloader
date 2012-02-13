#include <WinAPI.au3>

_Main()

Func _Main()
	Local $hwnd = GUICreate("test")
	Local $iX = _WinAPI_GetMousePosX()
	Local $iX2 = _WinAPI_GetMousePosX(True, $hwnd)
	Local $iY = _WinAPI_GetMousePosY()
	Local $iY2 = _WinAPI_GetMousePosY(True, $hwnd)

	MsgBox(4096, "Mouse Pos", "X = " & $iX & @LF & "Y = " & $iY & @LF & @LF & _
			"Client" & @LF & "X = " & $iX2 & @LF & "Y = " & $iY2)
EndFunc   ;==>_Main
