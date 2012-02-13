#include <GUIConstantsEx.au3>
#include <GuiButton.au3>
#include <WindowsConstants.au3>
#include <GuiMenu.au3>

Global $btn, $iMemo

; Note the controlId from these button(s) can NOT be read with GuiCtrlRead

_Main()

Func _Main()
	Local $hGUI

	$hGUI = GUICreate("Buttons", 400, 400)
	$iMemo = GUICtrlCreateEdit("", 10, 65, 390, 325, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")

	$btn = _GUICtrlButton_Create($hGUI, "Button1", 10, 10, 160, 40, BitOR($BS_COMMANDLINK, $BS_DEFPUSHBUTTON, $BS_PUSHLIKE))
	_GUICtrlButton_SetNote($btn, "This is a test of Vista")

	GUISetState()

	MemoWrite("Note: " & _GUICtrlButton_GetNote($btn))
	MemoWrite("Note Length: " & _GUICtrlButton_GetNoteLength($btn))
	MemoWrite("Button Text: " & _GUICtrlButton_GetText($btn))

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
		EndSwitch
	WEnd

	Exit

EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
