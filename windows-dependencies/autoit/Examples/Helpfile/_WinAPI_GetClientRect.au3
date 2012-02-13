#include <WinAPI.au3>

_Main()

Func _Main()
	Local $hwnd, $tRect
	$hwnd = GUICreate("test")
	$tRect = _WinAPI_GetClientRect($hwnd)
	MsgBox(4096, "Rect", _
			"Left..: " & DllStructGetData($tRect, "Left") & @LF & _
			"Right.: " & DllStructGetData($tRect, "Right") & @LF & _
			"Top...: " & DllStructGetData($tRect, "Top") & @LF & _
			"Bottom: " & DllStructGetData($tRect, "Bottom"))
EndFunc   ;==>_Main
