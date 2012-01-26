#include <GUIConstantsEx.au3>
#include <GuiListView.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $aItem, $hListView

	GUICreate("ListView Get Item", 400, 300)

	$hListView = GUICtrlCreateListView("Items", 2, 2, 394, 268)
	GUISetState()

	GUICtrlCreateListViewItem("Row 1", $hListView)
	GUICtrlCreateListViewItem("Row 2", $hListView)
	GUICtrlCreateListViewItem("Row 3", $hListView)

	; Show item 2 text
	$aItem = _GUICtrlListView_GetItem($hListView, 1)
	MsgBox(4160, "Information", "Item 2 Text: " & $aItem[3])

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

	GUIDelete()
EndFunc   ;==>_Main
