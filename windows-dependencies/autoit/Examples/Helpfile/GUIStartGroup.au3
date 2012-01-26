#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $button_1, $radio_1, $radio_2, $radio_3
	Local $radio_4, $radio_5, $radio_6, $input_1, $input_2
	Local $radioval1, $radioval2, $msg

	Opt("GUICoordMode", 1)

	GUICreate("Radio Box Grouping Demo", 400, 280)

	; Create the controls
	$button_1 = GUICtrlCreateButton("B&utton 1", 30, 20, 120, 40)
	GUICtrlCreateGroup("Group 1", 30, 90, 165, 160)
	GUIStartGroup()
	$radio_1 = GUICtrlCreateRadio("Radio &0", 50, 120, 70, 20)
	$radio_2 = GUICtrlCreateRadio("Radio &1", 50, 150, 60, 20)
	$radio_3 = GUICtrlCreateRadio("Radio &2", 50, 180, 60, 20)
	GUIStartGroup()
	$radio_4 = GUICtrlCreateRadio("Radio &A", 120, 120, 70, 20)
	$radio_5 = GUICtrlCreateRadio("Radio &B", 120, 150, 60, 20)
	$radio_6 = GUICtrlCreateRadio("Radio &C", 120, 180, 60, 20)
	GUIStartGroup()
	$input_1 = GUICtrlCreateInput("Input 1", 200, 20, 160, 30)
	$input_2 = GUICtrlCreateInput("Input 2", 200, 70, 160, 30)

	; Set the defaults (radio buttons clicked, default button, etc)
	GUICtrlSetState($radio_1, $GUI_CHECKED)
	GUICtrlSetState($radio_6, $GUI_CHECKED)
	GUICtrlSetState($button_1, $GUI_FOCUS + $GUI_DEFBUTTON)

	; Init our vars that we will use to keep track of radio events
	$radioval1 = 0 ; We will assume 0 = first radio button selected, 2 = last button
	$radioval2 = 2

	GUISetState()

	; In this message loop we use variables to keep track of changes to the radios, another
	; way would be to use GUICtrlRead() at the end to read in the state of each control.  Both
	; methods are equally valid
	While 1
		$msg = GUIGetMsg()
		Select
			Case $msg = $GUI_EVENT_CLOSE
				Exit

			Case $msg = $button_1
				MsgBox(0, "Button", "Radio " & $radioval1 & @LF & "Radio " & Chr($radioval2 + Asc("A")) & @LF & GUICtrlRead($input_1) & @LF & GUICtrlRead($input_2))

			Case $msg = $radio_1 Or $msg = $radio_2 Or $msg = $radio_3
				$radioval1 = $msg - $radio_1

			Case $msg = $radio_4 Or $msg = $radio_5 Or $msg = $radio_6
				$radioval2 = $msg - $radio_4

		EndSelect
	WEnd
EndFunc   ;==>Example
