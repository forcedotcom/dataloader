#include <GUIConstantsEx.au3>
#include <ButtonConstants.au3>

Example()

Func Example()
	Local $msg

	GUICreate("My GUI") ; will create a dialog box that when displayed is centered

	GUICtrlCreateButton("my picture button", 10, 20, 40, 40, $BS_ICON)
	GUICtrlSetImage(-1, "shell32.dll", 22)

	GUISetState()

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()

		If $msg = $GUI_EVENT_CLOSE Then ExitLoop
	WEnd
EndFunc   ;==>Example
