#include <GuiButton.au3>
#include <GUIConstantsEx.au3>
#include <GuiMenu.au3>
#include <WindowsConstants.au3>

Global $iMemo

; Note the controlID from these button(s) can NOT be read with GUICtrlRead.

_Main()

Func _Main()
	Local $hButton, $hGUI

	$hGUI = GUICreate("Buttons", 400, 400)
	$iMemo = GUICtrlCreateEdit("", 10, 75, 390, 315, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")

	$hButton = _GUICtrlButton_Create($hGUI, "Button1", 10, 10, 160, 60, BitOR($BS_COMMANDLINK, $BS_DEFCOMMANDLINK))
	_GUICtrlButton_SetNote($hButton, "This is a test of Vista")

	GUISetState()

	MemoWrite("Note: " & _GUICtrlButton_GetNote($hButton))
	MemoWrite("Note Length: " & _GUICtrlButton_GetNoteLength($hButton))
	MemoWrite("Button Text: " & _GUICtrlButton_GetText($hButton))

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
		EndSwitch
	WEnd

	Exit

EndFunc   ;==>_Main

; Write a line to the memo control.
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
