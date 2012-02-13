#include <GuiComboBoxEx.au3>
#include <GUIConstantsEx.au3>

$Debug_CB = False ; Check ClassName being passed to ComboBox/ComboBoxEx functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hGUI, $hCombo

	; Create GUI
	$hGUI = GUICreate("ComboBoxEx Init Storage", 400, 300)
	$hCombo = _GUICtrlComboBoxEx_Create($hGUI, "", 2, 2, 396, 296, $CBS_SIMPLE)
	GUISetState()

	MsgBox(4160, "Information", "Innit Storage Pre-Allocated Memory For: " & _GUICtrlComboBoxEx_InitStorage($hCombo, 150, 300) & " Items")
	_GUICtrlComboBoxEx_BeginUpdate($hCombo)

	For $x = 0 To 149
		_GUICtrlComboBoxEx_AddString($hCombo, StringFormat("%03d : Random string", Random(1, 100, 1)))
	Next
	_GUICtrlComboBoxEx_EndUpdate($hCombo)

	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main
