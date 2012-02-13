#include <WinAPI.au3>

_Main()

Func _Main()
	Local $button
	GUICreate("test")
	$button = GUICtrlCreateButton("testing", 0, 0)
	MsgBox(4096, "ID", "Dialog Control ID: " & _WinAPI_GetDlgCtrlID(GUICtrlGetHandle($button)))
EndFunc   ;==>_Main
