#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $msg

	Opt("GUICoordMode", 2) ; relative to cell mode

	GUICreate("My GUI Set Coord", 200, 100)
	GUICtrlCreateCheckbox("Check #1", 20, 10, 75)
	GUICtrlCreateCheckbox("Notify #2", 10, -1) ; next cell in the line

	GUISetCoord(20, 60)

	GUICtrlCreateButton("OK #3", -1, -1)
	GUICtrlCreateButton("Cancel #4", 10, -1)
	GUICtrlSetState(-1, $GUI_FOCUS)

	GUISetState() ; will display an empty dialog box

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()

		If $msg = $GUI_EVENT_CLOSE Then ExitLoop
	WEnd
EndFunc   ;==>Example
