#include <GUIConstantsEx.au3>
#include <GuiListView.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $aInfo, $hListView

	GUICreate("ListView Get Column", 400, 300)
	$hListView = GUICtrlCreateListView("col1|col2|col3", 2, 2, 394, 268)
	_GUICtrlListView_SetExtendedListViewStyle($hListView, BitOR($LVS_EX_FULLROWSELECT, $LVS_EX_CHECKBOXES))
	_GUICtrlListView_SetColumnWidth($hListView, 0, 100)
	GUISetState()

	GUICtrlCreateListViewItem("index 0|data1|more1", $hListView)
	GUICtrlCreateListViewItem("index 1|data2|more2", $hListView)
	GUICtrlCreateListViewItem("index 2|data3|more3", $hListView)
	GUICtrlCreateListViewItem("index 3|data4|more4", $hListView)
	GUICtrlCreateListViewItem("index 4|data5|more5", $hListView)

	; Change column
	$aInfo = _GUICtrlListView_GetColumn($hListView, 0)
	MsgBox(4160, "Information", "Column 1 Width: " & $aInfo[4])
	_GUICtrlListView_SetColumn($hListView, 0, "New Column 1", 150)
	$aInfo = _GUICtrlListView_GetColumn($hListView, 0)
	MsgBox(4160, "Information", "Column 1 Width: " & $aInfo[4])

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

	GUIDelete()
EndFunc   ;==>_Main
