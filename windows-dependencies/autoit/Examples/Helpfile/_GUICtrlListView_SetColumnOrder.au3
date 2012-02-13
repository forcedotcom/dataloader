#include <GUIConstantsEx.au3>
#include <GuiListView.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $a_order, $hListView

	GUICreate("ListView Set Column Order", 400, 300)
	$hListView = GUICtrlCreateListView("Column 1|Column 2|Column 3|Column 4", 2, 2, 394, 268)
	GUISetState()

	; Set column order
	MsgBox(4160, "Information", "Changing column order")
	_GUICtrlListView_SetColumnOrder($hListView, "3|2|0|1")

	; Show column order
	$a_order = _GUICtrlListView_GetColumnOrderArray($hListView)
	MsgBox(4160, "Information", StringFormat("Column order: [%d, %d, %d, %d]", $a_order[1], $a_order[2], $a_order[3], $a_order[4]))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

	GUIDelete()
EndFunc   ;==>_Main
