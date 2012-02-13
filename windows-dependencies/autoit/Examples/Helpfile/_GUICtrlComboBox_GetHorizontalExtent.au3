#include <GuiComboBox.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

$Debug_CB = False ; Check ClassName being passed to ComboBox/ComboBoxEx functions, set to True and use a handle to another control to see it work

Global $iMemo

_Main()

Func _Main()
	Local $hCombo

	; Create GUI
	GUICreate("ComboBox Get Horizontal Extent", 400, 296)
	$hCombo = GUICtrlCreateCombo("", 2, 2, 396, 296, BitOR($CBS_SIMPLE, $CBS_DISABLENOSCROLL, $WS_HSCROLL))
	GUISetState()

	; Add files
	_GUICtrlComboBox_BeginUpdate($hCombo)
	_GUICtrlComboBox_AddDir($hCombo, @WindowsDir & "\*.exe")
	_GUICtrlComboBox_EndUpdate($hCombo)

	; Get Horizontal Extent
	MsgBox(4160, "Information", "Horizontal Extent: " & _GUICtrlComboBox_GetHorizontalExtent($hCombo))

	; Set Horizontal Extent
	_GUICtrlComboBox_SetHorizontalExtent($hCombo, 500)

	; Get Horizontal Extent
	MsgBox(4160, "Information", "Horizontal Extent: " & _GUICtrlComboBox_GetHorizontalExtent($hCombo))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
