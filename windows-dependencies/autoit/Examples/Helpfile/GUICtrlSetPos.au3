#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $right, $label, $button, $msg

	GUICreate("My GUI position") ; will create a dialog box that when displayed is centered

	$right = 0
	$label = GUICtrlCreateLabel("my moving label", 10, 20)

	$button = GUICtrlCreateButton("Click to close", 50, 50)
	GUICtrlSetState(-1, $GUI_FOCUS) ; the focus is on this button

	GUISetState()

	While 1
		$msg = GUIGetMsg()

		If $msg = $button Or $msg = $GUI_EVENT_CLOSE Then Exit
		If $right = 0 Then
			$right = 1
			GUICtrlSetPos($label, 20, 20)
		Else
			$right = 0
			GUICtrlSetPos($label, 10, 20)
		EndIf
		Sleep(100)
	WEnd
EndFunc   ;==>Example
