#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $msg

	GUICreate("My GUI color text") ; will create a dialog box that when displayed is centered

	GUICtrlCreateLabel("my Red label", 10, 20)
	GUICtrlSetColor(-1, 0xff0000) ; Red

	GUISetState()

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()

		If $msg = $GUI_EVENT_CLOSE Then ExitLoop
	WEnd
EndFunc   ;==>Example
