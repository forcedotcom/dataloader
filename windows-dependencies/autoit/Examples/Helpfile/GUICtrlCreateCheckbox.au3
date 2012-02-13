#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $msg
	GUICreate("My GUI Checkbox") ; will create a dialog box that when displayed is centered

	GUICtrlCreateCheckbox("CHECKBOX 1", 10, 10, 120, 20)

	GUISetState() ; will display an  dialog box with 1 checkbox

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()

		If $msg = $GUI_EVENT_CLOSE Then ExitLoop
	WEnd
EndFunc   ;==>Example
