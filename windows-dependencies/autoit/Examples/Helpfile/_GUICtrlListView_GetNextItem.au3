#include <GUIConstantsEx.au3>
#include <GuiListView.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hListView

	GUICreate("ListView Get Next Item", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
	GUISetState()

	; Add columns
	_GUICtrlListView_AddColumn($hListView, "Items", 100)

	; Add items
	_GUICtrlListView_AddItem($hListView, "Item 1")
	_GUICtrlListView_AddItem($hListView, "Item 2")
	_GUICtrlListView_AddItem($hListView, "Item 3")

	; Select item 2
	_GUICtrlListView_SetItemSelected($hListView, 1)

	; Find selected item
	MsgBox(4160, "Information", "Selected Item: " & _GUICtrlListView_GetNextItem($hListView))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
