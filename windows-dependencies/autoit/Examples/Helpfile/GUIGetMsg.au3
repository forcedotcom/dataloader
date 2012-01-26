#include <GUIConstantsEx.au3>

Example()

;-------------------------------------------------------------------------------------
; Example - Press the button to see the value of the radio boxes
; The script also detects state changes (closed/minimized/timeouts, etc).
Func Example()
	Local $button_1, $radio_1, $radio_3
	Local $radioval1, $msg

	Opt("GUICoordMode", 1)
	GUICreate("Radio Box Demo", 400, 280)

	; Create the controls
	$button_1 = GUICtrlCreateButton("B&utton 1", 30, 20, 120, 40)
	GUICtrlCreateGroup("Group 1", 30, 90, 165, 160)
	GUIStartGroup()
	$radio_1 = GUICtrlCreateRadio("Radio &0", 50, 120, 70, 20)
	GUICtrlCreateRadio("Radio &1", 50, 150, 60, 20)
	$radio_3 = GUICtrlCreateRadio("Radio &2", 50, 180, 60, 20)

	; Init our vars that we will use to keep track of GUI events
	$radioval1 = 0 ; We will assume 0 = first radio button selected, 2 = last button

	; Show the GUI
	GUISetState()

	; In this message loop we use variables to keep track of changes to the radios, another
	; way would be to use GUICtrlRead() at the end to read in the state of each control
	While 1
		$msg = GUIGetMsg()
		Select
			Case $msg = $GUI_EVENT_CLOSE
				MsgBox(0, "", "Dialog was closed")
				Exit
			Case $msg = $GUI_EVENT_MINIMIZE
				MsgBox(0, "", "Dialog minimized", 2)
			Case $msg = $GUI_EVENT_MAXIMIZE
				MsgBox(0, "", "Dialog restored", 2)

			Case $msg = $button_1
				MsgBox(0, "Default button clicked", "Radio " & $radioval1)

			Case $msg >= $radio_1 And $msg <= $radio_3
				$radioval1 = $msg - $radio_1

		EndSelect
	WEnd
EndFunc   ;==>Example
