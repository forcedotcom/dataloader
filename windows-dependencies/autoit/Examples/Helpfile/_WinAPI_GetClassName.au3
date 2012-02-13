#include <WinAPI.au3>

_Main()

Func _Main()
	Local $hwnd
	$hwnd = GUICreate("test")
	MsgBox(4096, "Get ClassName", "ClassName of " & $hwnd & ": " & _WinAPI_GetClassName($hwnd))
EndFunc   ;==>_Main
