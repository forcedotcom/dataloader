#include <GUIConstantsEx.au3>
#include <ButtonConstants.au3>

_Main()

Func _Main()
	Local $button1, $button2, $button3, $button4
	Local $button5, $buttonclose

	GUICreate("test", 240, 180)
	$button1 = GUICtrlCreateButton("1", 0, 0, 40, 40, $BS_ICON)
	GUICtrlSetImage(-1, "shell32.dll", 5)
	$button2 = GUICtrlCreateButton("2", 40, 00, 40, 40, $BS_ICON)
	GUICtrlSetImage(-1, "shell32.dll", 7)
	$button3 = GUICtrlCreateButton("3", 80, 00, 40, 40, $BS_ICON)
	GUICtrlSetImage(-1, "shell32.dll", 22)
	$button4 = GUICtrlCreateButton("4", 120, 0, 40, 40, $BS_ICON)
	GUICtrlSetImage(-1, "shell32.dll", 23)
	$button5 = GUICtrlCreateButton("5", 160, 0, 40, 40, $BS_ICON)
	GUICtrlSetImage(-1, "shell32.dll", 32)
	$buttonclose = GUICtrlCreateButton("close", 200, 0, 40, 40, $BS_ICON)
	GUICtrlSetImage(-1, "shell32.dll", 28)
	GUISetState()

	; Run the GUI until the dialog is closed
	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
			Case $button1
				;
			Case $button2
				;
			Case $button3
				;
			Case $button4
				;
			Case $button5
				;
			Case $buttonclose
				ExitLoop
			Case Else
		EndSwitch
	WEnd

	GUIDelete()
EndFunc   ;==>_Main
