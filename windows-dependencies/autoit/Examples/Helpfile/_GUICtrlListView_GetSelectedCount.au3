#include <GUIConstantsEx.au3>
#include <GuiListView.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hListView

	GUICreate("ListView Get Selected Count", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268, BitOR($LVS_SHOWSELALWAYS, $LVS_REPORT))
	GUISetState()

	; Add columns
	_GUICtrlListView_AddColumn($hListView, "Column 1", 100)

	; Add items
	_GUICtrlListView_AddItem($hListView, "Item 1")
	_GUICtrlListView_AddItem($hListView, "Item 2")
	_GUICtrlListView_AddItem($hListView, "Item 3")

	; Select multiple items
	_GUICtrlListView_SetItemSelected($hListView, 1)
	_GUICtrlListView_SetItemSelected($hListView, 2)
	MsgBox(4160, "Information", "Selected Count: " & _GUICtrlListView_GetSelectedCount($hListView))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
