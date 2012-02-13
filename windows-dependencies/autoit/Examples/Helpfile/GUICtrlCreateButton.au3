#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $Button_1, $Button_2, $msg
	GUICreate("My GUI Button") ; will create a dialog box that when displayed is centered

	Opt("GUICoordMode", 2)
	$Button_1 = GUICtrlCreateButton("Run Notepad", 10, 30, 100)
	$Button_2 = GUICtrlCreateButton("Button Test", 0, -1)

	GUISetState() ; will display an  dialog box with 2 button

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()
		Select
			Case $msg = $GUI_EVENT_CLOSE
				ExitLoop
			Case $msg = $Button_1
				Run('notepad.exe') ; Will Run/Open Notepad
			Case $msg = $Button_2
				MsgBox(0, 'Testing', 'Button 2 was pressed') ; Will demonstrate Button 2 being pressed
		EndSelect
	WEnd
EndFunc   ;==>Example
