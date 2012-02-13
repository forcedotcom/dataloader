#include <GUIConstantsEx.au3>
#include <GuiListView.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hListView

	GUICreate("ListView Get Selected Column", 400, 300)
	$hListView = GUICtrlCreateListView("Column 1|Column 2|Column 3", 2, 2, 394, 268)
	GUICtrlCreateListViewItem("line1|data1|more1", $hListView)
	GUICtrlCreateListViewItem("line2|data2|more2", $hListView)
	GUICtrlCreateListViewItem("line3|data3|more3", $hListView)
	GUICtrlCreateListViewItem("line4|data4|more4", $hListView)
	GUICtrlCreateListViewItem("line5|data5|more5", $hListView)
	GUISetState()

	; Select column 2
	_GUICtrlListView_SetSelectedColumn($hListView, 1)
	MsgBox(4160, "Information", "Selected Column: " & _GUICtrlListView_GetSelectedColumn($hListView))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
