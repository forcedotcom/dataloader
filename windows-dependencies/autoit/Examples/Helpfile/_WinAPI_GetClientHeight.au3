#include <WinAPI.au3>

_Main()

Func _Main()
	Local $hwnd
	$hwnd = GUICreate("test")
	MsgBox(4096, "Client", "Client Height: " & _WinAPI_GetClientHeight($hwnd))
EndFunc   ;==>_Main
