#include <WinAPI.au3>

_Main()

Func _Main()
	Local $hwnd, $button
	$hwnd = GUICreate("test")
	$button = GUICtrlCreateButton("button", 0, 0)
	MsgBox(4096, "Handle", "Get Dialog Item: " & _WinAPI_GetDlgItem($hwnd, $button))
EndFunc   ;==>_Main
