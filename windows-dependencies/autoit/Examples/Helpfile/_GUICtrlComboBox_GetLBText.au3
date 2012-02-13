#include <GuiComboBox.au3>
#include <GUIConstantsEx.au3>

$Debug_CB = False ; Check ClassName being passed to ComboBox/ComboBoxEx functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $sText, $hCombo

	; Create GUI
	GUICreate("ComboBox Get LB Text", 400, 296)
	$hCombo = GUICtrlCreateCombo("", 2, 2, 396, 296)
	GUISetState()

	; Add files
	_GUICtrlComboBox_BeginUpdate($hCombo)
	_GUICtrlComboBox_AddDir($hCombo, @WindowsDir & "\*.exe")
	_GUICtrlComboBox_EndUpdate($hCombo)

	; Get LB Text
	_GUICtrlComboBox_GetLBText($hCombo, 2, $sText)
	MsgBox(4160, "Information", "LB Text: " & $sText)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
