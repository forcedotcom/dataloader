#include <GuiComboBoxEx.au3>
#include <GUIConstantsEx.au3>
#include <Constants.au3>

$Debug_CB = False ; Check ClassName being passed to ComboBox/ComboBoxEx functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hGUI, $tInfo, $hCombo

	; Create GUI
	$hGUI = GUICreate("ComboBoxEx Get ComboBox Info", 400, 300)
	$hCombo = _GUICtrlComboBoxEx_Create($hGUI, "", 2, 2, 394, 100)
	GUISetState()

	; Add files
	_GUICtrlComboBoxEx_AddDir($hCombo, "", $DDL_DRIVES, False)
	_GUICtrlComboBoxEx_AddDir($hCombo, "", $DDL_DRIVES)
	_GUICtrlComboBoxEx_BeginUpdate($hCombo)
	_GUICtrlComboBoxEx_AddDir($hCombo, @WindowsDir & "\*.exe")
	_GUICtrlComboBoxEx_EndUpdate($hCombo)

	If _GUICtrlComboBoxEx_GetComboBoxInfo($hCombo, $tInfo) Then _
			MsgBox(4160, "Information", StringFormat("Edit Rect [%d][%d][%d][%d]", _
			DllStructGetData($tInfo, "EditLeft"), DllStructGetData($tInfo, "EditTop"), _
			DllStructGetData($tInfo, "EditRight"), DllStructGetData($tInfo, "EditBottom")))

	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main
