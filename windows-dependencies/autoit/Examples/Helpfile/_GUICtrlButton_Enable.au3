#include <GUIConstantsEx.au3>
#include <GuiButton.au3>
#include <WindowsConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $rdo, $rdo2, $chk

	GUICreate("Buttons", 400, 400)
	$iMemo = GUICtrlCreateEdit("", 119, 10, 276, 374, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")

	$rdo = GUICtrlCreateRadio("Radio1", 10, 10, 90, 50)

	$rdo2 = GUICtrlCreateRadio("Radio2", 10, 60, 90, 50)
	_GUICtrlButton_SetCheck($rdo2)

	$chk = GUICtrlCreateCheckbox("Check1", 10, 120, 90, 50, BitOR($BS_AUTO3STATE, $BS_NOTIFY))
	_GUICtrlButton_SetCheck($chk, $BST_INDETERMINATE)

	GUISetState()

	MemoWrite("Disabled Button: " & _GUICtrlButton_Enable($rdo2, False))
	Sleep(1000)
	MemoWrite("Enabled Button: " & _GUICtrlButton_Enable($rdo2))

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
			Case $rdo
				MemoWrite("Clicked $rdo")
			Case $rdo2
				MemoWrite("Clicked $rdo2")
			Case $chk
				MemoWrite("Clicked $chk")
		EndSwitch
	WEnd

	Exit
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
