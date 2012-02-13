#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $msg

	GUICreate("My GUI state") ; will create a dialog box that when displayed is centered

	GUICtrlCreateLabel("my disable label", 10, 20)
	GUICtrlSetState(-1, $GUI_DISABLE) ; the label is in disable state

	GUICtrlCreateButton("my button", 50, 50)
	GUICtrlSetState(-1, $GUI_FOCUS) ; the focus is on this button

	GUISetState()

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()

		If $msg = $GUI_EVENT_CLOSE Then ExitLoop
	WEnd
EndFunc   ;==>Example
