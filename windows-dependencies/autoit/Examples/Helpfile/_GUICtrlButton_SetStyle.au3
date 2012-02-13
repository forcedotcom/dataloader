#include <GUIConstantsEx.au3>
#include <GuiButton.au3>
#include <WindowsConstants.au3>

_Main()

Func _Main()
	Local $btn, $btn2

	GUICreate("Buttons", 400, 400)
	GUISetState()

	$btn = GUICtrlCreateButton("Button1", 10, 10, 90, 50)

	$btn2 = GUICtrlCreateButton("Button2", 10, 70, 90, 50)

	MsgBox(4096, "Information", "Setting Button Style")
	_GUICtrlButton_SetStyle($btn, $BS_AUTORADIOBUTTON)
	_GUICtrlButton_SetStyle($btn2, $BS_AUTOCHECKBOX)

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
		EndSwitch
	WEnd

	Exit
EndFunc   ;==>_Main
