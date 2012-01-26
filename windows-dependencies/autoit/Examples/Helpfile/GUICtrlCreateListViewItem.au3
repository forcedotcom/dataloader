#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

Example()

Func Example()
	Local $listview, $button, $item1, $item2, $item3, $msg

	GUICreate("listview items", 220, 250, 100, 200, -1, $WS_EX_ACCEPTFILES)
	GUISetBkColor(0x00E0FFFF) ; will change background color

	$listview = GUICtrlCreateListView("col1  |col2|col3  ", 10, 10, 200, 150);,$LVS_SORTDESCENDING)
	$button = GUICtrlCreateButton("Value?", 75, 170, 70, 20)
	$item1 = GUICtrlCreateListViewItem("item2|col22|col23", $listview)
	$item2 = GUICtrlCreateListViewItem("............item1|col12|col13", $listview)
	$item3 = GUICtrlCreateListViewItem("item3|col32|col33", $listview)
	GUICtrlCreateInput("", 20, 200, 150)
	GUICtrlSetState(-1, $GUI_DROPACCEPTED) ; to allow drag and dropping
	GUISetState()
	GUICtrlSetData($item2, "|ITEM1")
	GUICtrlSetData($item3, "||COL33")
	GUICtrlDelete($item1)

	Do
		$msg = GUIGetMsg()

		Select
			Case $msg = $button
				MsgBox(0, "listview item", GUICtrlRead(GUICtrlRead($listview)), 2)
			Case $msg = $listview
				MsgBox(0, "listview", "clicked=" & GUICtrlGetState($listview), 2)
		EndSelect
	Until $msg = $GUI_EVENT_CLOSE
EndFunc   ;==>Example
