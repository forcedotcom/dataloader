#include <GUIConstantsEx.au3>

Global $x, $y

Example()

Func Example()
	Local $msg

	HotKeySet("{Esc}", "GetPos")

	GUICreate("Press Esc to Get Pos", 400, 400)
	$x = GUICtrlCreateLabel("0", 10, 10, 50)
	$y = GUICtrlCreateLabel("0", 10, 30, 50)
	GUISetState()

	; Run the GUI until the dialog is closed
	Do
		$msg = GUIGetMsg()
	Until $msg = $GUI_EVENT_CLOSE
EndFunc   ;==>Example

Func GetPos()
	Local $a

	$a = GUIGetCursorInfo()
	GUICtrlSetData($x, $a[0])
	GUICtrlSetData($y, $a[1])
EndFunc   ;==>GetPos
