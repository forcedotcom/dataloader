#include <GUIConstantsEx.au3>

Global $IDC = -1, $newIDC = 0

Example()

Func Example()

	HotKeySet("{Esc}", "Increment")

	GUICreate("Press Esc to Increment", 400, 400, 0, 0, 0x04CF0000, 0x00000110)

	GUISetState()

	While GUIGetMsg() <> $GUI_EVENT_CLOSE
		If $newIDC <> $IDC Then
			$IDC = $newIDC
			GUISetCursor($IDC)
		EndIf
		ToolTip("GUI Cursor #" & $IDC)
	WEnd
EndFunc   ;==>Example

Func Increment()
	$newIDC = $IDC + 1
	If $newIDC > 15 Then $newIDC = 0
EndFunc   ;==>Increment
