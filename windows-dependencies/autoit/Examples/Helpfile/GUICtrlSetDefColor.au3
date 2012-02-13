#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $msg

	GUICreate("test GUISetTextColor", 100, 100) ; will create a dialog box that when displayed is centered

	GUICtrlSetDefColor(0xFF0000) ; will change text color for all defined controls

	GUICtrlCreateLabel("label", 10, 5)

	GUICtrlCreateRadio("radio", 10, 25, 50)
	GUICtrlSetColor(-1, 0x0000FF) ; will change text color for specified control

	GUICtrlCreateButton("button", 10, 55)

	GUISetState() ; will display an empty dialog box

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()

		If $msg = $GUI_EVENT_CLOSE Then ExitLoop
	WEnd
EndFunc   ;==>Example
