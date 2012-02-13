#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $radio1, $radio2, $msg
	GUICreate("My GUI radio") ; will create a dialog box that when displayed is centered

	$radio1 = GUICtrlCreateRadio("Radio 1", 10, 10, 120, 20)
	$radio2 = GUICtrlCreateRadio("Radio 2", 10, 40, 120, 20)
	GUICtrlSetState($radio2, $GUI_CHECKED)

	GUISetState() ; will display an  dialog box with 1 checkbox

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()
		Select
			Case $msg = $GUI_EVENT_CLOSE
				ExitLoop
			Case $msg = $radio1 And BitAND(GUICtrlRead($radio1), $GUI_CHECKED) = $GUI_CHECKED
				MsgBox(64, 'Info:', 'You clicked the Radio 1 and it is Checked.')
			Case $msg = $radio2 And BitAND(GUICtrlRead($radio2), $GUI_CHECKED) = $GUI_CHECKED
				MsgBox(64, 'Info:', 'You clicked on Radio 2 and it is Checked.')
		EndSelect
	WEnd
EndFunc   ;==>Example
