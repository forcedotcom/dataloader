#include <GUIConstantsEx.au3>
#include <GuiListView.au3>


Example()

Func Example()
	Local $listview, $msg, $iIndex

	GUICreate("listview items", 220, 250, 100, 200)
	GUISetBkColor(0x00E0FFFF) ; will change background color

	$listview = GUICtrlCreateListView("col1  |col2|col3  ", 10, 10, 200, 150);,$LVS_SORTDESCENDING)
	For $x = 1 To 30
		GUICtrlCreateListViewItem("item" & $x & "|col" & $x & "2|col" & $x & "3", $listview)
	Next
	GUISetState()

	$iIndex = Random(0, 29, 1)
	MsgBox(4160, "Information", $iIndex & " is Visible: " & _GUICtrlListView_IsItemVisible($listview, $iIndex))

	Do
		$msg = GUIGetMsg()
	Until $msg = $GUI_EVENT_CLOSE
EndFunc   ;==>Example
