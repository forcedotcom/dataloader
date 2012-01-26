#include <GUIConstantsEx.au3>
#include <GuiButton.au3>
#include <WindowsConstants.au3>

_Main()

Func _Main()
	Local $y = 70, $btn[6], $rdo[6], $chk[6]

	GUICreate("Buttons", 510, 400)
	GUISetState()

	$btn[0] = GUICtrlCreateButton("Button1", 10, 10, 90, 50)

	$rdo[0] = GUICtrlCreateRadio("Radio Button1", 120, 10, 120, 25)

	$chk[0] = GUICtrlCreateCheckbox("Check Button1", 260, 10, 120, 25)

	For $x = 1 To 5
		$btn[$x] = GUICtrlCreateButton("Button" & $x + 1, 10, $y, 90, 50)
		$rdo[$x] = GUICtrlCreateRadio("Radio Button" & $x + 1, 120, $y, 120, 25)
		$chk[$x] = GUICtrlCreateCheckbox("Check Button" & $x + 1, 260, $y, 120, 25)
		$y += 60
	Next

	; Hide the buttons
	For $x = 0 To 5
		_GUICtrlButton_Show($btn[$x], False)
		_GUICtrlButton_Show($rdo[$x], False)
		_GUICtrlButton_Show($chk[$x], False)
		Sleep(500)
	Next

	; Show the buttons
	For $x = 5 To 0 Step -1
		_GUICtrlButton_Show($chk[$x])
		_GUICtrlButton_Show($rdo[$x])
		_GUICtrlButton_Show($btn[$x])
		Sleep(500)
	Next

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
		EndSwitch
	WEnd

	Exit
EndFunc   ;==>_Main
