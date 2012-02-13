#include <GuiComboBox.au3>
#include <GUIConstantsEx.au3>

$Debug_CB = False ; Check ClassName being passed to ComboBox/ComboBoxEx functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hCombo

	; Create GUI
	GUICreate("ComboBox Set Min Visible", 400, 296)
	$hCombo = GUICtrlCreateCombo("", 2, 2, 396, 296)
	GUISetState()

	; Add files
	_GUICtrlComboBox_BeginUpdate($hCombo)
	_GUICtrlComboBox_AddDir($hCombo, @WindowsDir & "\*.exe")
	_GUICtrlComboBox_EndUpdate($hCombo)

	; Get Min Visible
	MsgBox(4160, "Information", "Min Visible:" & @TAB & _GUICtrlComboBox_GetMinVisible($hCombo), 3)

	; show drop down
	_GUICtrlComboBox_ShowDropDown($hCombo, True)

	Sleep(500)

	; Set Min Visible
	_GUICtrlComboBox_SetMinVisible($hCombo, 50)

	; show drop down
	_GUICtrlComboBox_ShowDropDown($hCombo)

	Sleep(500)

	_GUICtrlComboBox_ShowDropDown($hCombo, True)

	Sleep(500)

	; Get Min Visible
	MsgBox(4160, "Information", "Min Visible:" & @TAB & _GUICtrlComboBox_GetMinVisible($hCombo), 3)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
