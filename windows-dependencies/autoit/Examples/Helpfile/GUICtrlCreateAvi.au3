#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $ani1, $buttonstart, $buttonstop, $msg

	GUICreate("My GUI Animation", 300, 200)
	$ani1 = GUICtrlCreateAvi(@SystemDir & "\shell32.dll", 165, 50, 10)

	$buttonstart = GUICtrlCreateButton("start", 50, 150, 70, 22)
	$buttonstop = GUICtrlCreateButton("stop", 150, 150, 70, 22)

	GUISetState()

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()

		Select
			Case $msg = $GUI_EVENT_CLOSE
				ExitLoop

			Case $msg = $buttonstart
				GUICtrlSetState($ani1, 1)

			Case $msg = $buttonstop
				GUICtrlSetState($ani1, 0)

		EndSelect
	WEnd
EndFunc   ;==>Example
