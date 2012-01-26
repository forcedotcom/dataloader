#include <GuiComboBox.au3>
#include <GUIConstantsEx.au3>
#include <Constants.au3>

$Debug_CB = False ; Check ClassName being passed to ComboBox/ComboBoxEx functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hCombo

	; Create GUI
	GUICreate("ComboBox Find String Exact", 400, 296)
	$hCombo = GUICtrlCreateCombo("", 2, 2, 396, 296)
	GUISetState()

	; Add files
	_GUICtrlComboBox_BeginUpdate($hCombo)
	_GUICtrlComboBox_AddDir($hCombo, "", $DDL_DRIVES, False)
	_GUICtrlComboBox_AddString($hCombo, "This is a test")
	_GUICtrlComboBox_AddDir($hCombo, "", $DDL_DRIVES)
	_GUICtrlComboBox_AddString($hCombo, "This is eXact")
	_GUICtrlComboBox_EndUpdate($hCombo)

	; Find string
	MsgBox(4160, "Information", "Find String: " & _GUICtrlComboBox_FindStringExact($hCombo, "This is eXact"))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
