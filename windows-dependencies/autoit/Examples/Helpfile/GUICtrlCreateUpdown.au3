#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

Example()

Func Example()
	Local $title, $input, $msg

	$title = "My GUI UpDown"
	GUICreate($title, -1, -1, -1, -1, $WS_SIZEBOX)

	$input = GUICtrlCreateInput("2", 10, 10, 50, 20)
	GUICtrlCreateUpdown($input)

	; Attempt to resize input control
	GUICtrlSetPos($input, 10, 10, 100, 40)

	GUISetState()

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()

		If $msg = $GUI_EVENT_CLOSE Then ExitLoop
	WEnd

	MsgBox(0, "Updown", GUICtrlRead($input))
EndFunc   ;==>Example
