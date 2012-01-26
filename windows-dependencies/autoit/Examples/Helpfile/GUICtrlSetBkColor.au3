#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $msg

	GUICreate("My GUI background color") ; will create a dialog box that when displayed is centered

	GUICtrlCreateLabel("my label", 10, 20)
	GUICtrlSetBkColor(-1, 0x00ff00) ; Green

	GUISetState()

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()

		If $msg = $GUI_EVENT_CLOSE Then ExitLoop
	WEnd
EndFunc   ;==>Example
