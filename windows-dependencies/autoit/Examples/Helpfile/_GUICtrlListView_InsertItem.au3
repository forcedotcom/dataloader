#include <GUIConstantsEx.au3>
#include <GuiListView.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hListView

	GUICreate("ListView Insert Item", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
	GUISetState()

	; Insert columns
	_GUICtrlListView_InsertColumn($hListView, 0, "Column 1", 100)

	; Add items
	_GUICtrlListView_InsertItem($hListView, "Item 1", 0)
	_GUICtrlListView_InsertItem($hListView, "Item 2", 1)
	_GUICtrlListView_InsertItem($hListView, "Item 3", 1)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
