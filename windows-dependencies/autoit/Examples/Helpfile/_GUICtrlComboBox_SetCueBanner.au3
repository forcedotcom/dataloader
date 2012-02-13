#include <GuiComboBox.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

$Debug_CB = False ; Check ClassName being passed to ComboBox/ComboBoxEx functions, set to True and use a handle to another control to see it work

Global $iMemo
_Main()

Func _Main()
	Local $hCombo

	; Create GUI
	GUICreate("ComboBox Get Count", 400, 296)
	$hCombo = GUICtrlCreateCombo("", 2, 2, 396, 296)
	_GUICtrlComboBox_SetCueBanner($hCombo, "Select an Item")
	$iMemo = GUICtrlCreateEdit("", 10, 50, 376, 234, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Add files
	_GUICtrlComboBox_BeginUpdate($hCombo)
	_GUICtrlComboBox_AddDir($hCombo, @WindowsDir & "\*.exe")
	_GUICtrlComboBox_EndUpdate($hCombo)

	MemoWrite("Cue Banner: " & _GUICtrlComboBox_GetCueBanner($hCombo))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
