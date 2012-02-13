#include <GUIConstantsEx.au3>
#include <GuiListView.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $aInfo, $hListView

	GUICreate("ListView Set Column", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
	GUISetState()

	; Add columns
	_GUICtrlListView_AddColumn($hListView, "Column 1", 100)
	_GUICtrlListView_AddColumn($hListView, "Column 2", 100)
	_GUICtrlListView_AddColumn($hListView, "Column 3", 100)

	; Change column
	$aInfo = _GUICtrlListView_GetColumn($hListView, 0)
	MsgBox(4160, "Information", "Column 1 Width: " & $aInfo[4])
	_GUICtrlListView_SetColumn($hListView, 0, "New Column 1", 150, 1)
	$aInfo = _GUICtrlListView_GetColumn($hListView, 0)
	MsgBox(4160, "Information", "Column 1 Width: " & $aInfo[4])

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
