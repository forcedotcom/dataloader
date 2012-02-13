#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $msg

	GUICreate("My GUI control tip") ; will create a dialog box that when displayed is centered

	GUICtrlCreateLabel("my label", 10, 20)
	GUICtrlSetTip(-1, "tip of my label")

	GUISetState()

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()

		If $msg = $GUI_EVENT_CLOSE Then ExitLoop
	WEnd
EndFunc   ;==>Example
