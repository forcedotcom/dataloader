#include <WinAPI.au3>

_Main()

Func _Main()
	Local $hwnd
	$hwnd = GUICreate("test")
	MsgBox(4096, "Client", "Client Width: " & _WinAPI_GetClientWidth($hwnd))
EndFunc   ;==>_Main
