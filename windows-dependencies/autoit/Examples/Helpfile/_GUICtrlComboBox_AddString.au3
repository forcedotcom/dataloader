#include <GuiComboBox.au3>
#include <GUIConstantsEx.au3>
#include <Constants.au3>

$Debug_CB = False ; Check ClassName being passed to ComboBox/ComboBoxEx functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hCombo

	; Create GUI
	GUICreate("ComboBox Add String", 400, 296)
	$hCombo = GUICtrlCreateCombo("", 2, 2, 396, 296)
	GUISetState()

	; Add files
	_GUICtrlComboBox_AddDir($hCombo, "", $DDL_DRIVES, False)

	; Add string
	_GUICtrlComboBox_AddString($hCombo, "This string has been added")

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
