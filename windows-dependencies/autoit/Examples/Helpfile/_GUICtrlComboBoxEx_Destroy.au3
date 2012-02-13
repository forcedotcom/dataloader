#include <GuiComboBoxEx.au3>
#include <GUIConstantsEx.au3>

$Debug_CB = False ; Check ClassName being passed to ComboBox/ComboBoxEx functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hGUI, $hCombo

	; Create GUI
	$hGUI = GUICreate("ComboBoxEx Destroy", 400, 300)
	$hCombo = _GUICtrlComboBoxEx_Create($hGUI, "This is a test|Line 2", 2, 2, 394, 268)
	GUISetState()

	_GUICtrlComboBoxEx_AddString($hCombo, "Some More Text")
	_GUICtrlComboBoxEx_InsertString($hCombo, "Inserted Text", 1)

	;Destroy control
	MsgBox(266256, "Information", "Destroy the control")
	_GUICtrlComboBoxEx_Destroy($hCombo)


	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main
