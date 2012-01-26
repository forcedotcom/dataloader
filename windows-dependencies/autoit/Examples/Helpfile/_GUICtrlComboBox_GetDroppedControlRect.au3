#include <GuiComboBox.au3>
#include <GUIConstantsEx.au3>
#include <Constants.au3>

$Debug_CB = False ; Check ClassName being passed to ComboBox/ComboBoxEx functions, set to True and use a handle to another control to see it work

Global $iMemo

_Main()

Func _Main()
	Local $aRect, $hCombo

	; Create GUI
	GUICreate("ComboBox Get Dropped Control Rect", 400, 296)
	$hCombo = GUICtrlCreateCombo("", 2, 2, 396, 296)
	$iMemo = GUICtrlCreateEdit("", 2, 32, 396, 266, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Add files
	_GUICtrlComboBox_BeginUpdate($hCombo)
	_GUICtrlComboBox_AddDir($hCombo, @WindowsDir & "\*.exe")
	_GUICtrlComboBox_EndUpdate($hCombo)

	; Get Dropped Control Rect
	$aRect = _GUICtrlComboBox_GetDroppedControlRect($hCombo)

	MemoWrite("X coordinate of the upper left corner ......: " & $aRect[0])
	MemoWrite("Y coordinate of the upper left corner ......: " & $aRect[1])
	MemoWrite("X coordinate of the lower right corner .....: " & $aRect[2])
	MemoWrite("Y coordinate of the lower right corner .....: " & $aRect[3])

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
