#include <GUIConstantsEx.au3>
#include <GuiListView.au3>
#include <WindowsConstants.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $tRect, $hListView

	GUICreate("ListView Get Item Rectangle Ex", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
	GUISetState()

	; Add columns
	_GUICtrlListView_AddColumn($hListView, "Items", 100)

	; Add items
	_GUICtrlListView_AddItem($hListView, "Item 1")
	_GUICtrlListView_AddItem($hListView, "Item 2")
	_GUICtrlListView_AddItem($hListView, "Item 3")

	; Get item 2 rectangle
	$tRect = _GUICtrlListView_GetItemRectEx($hListView, 1)
	MsgBox(4160, "Information", StringFormat("Item 2 Rectangle : [%d, %d, %d, %d]", DllStructGetData($tRect, "Left"), _
			DllStructGetData($tRect, "Top"), DllStructGetData($tRect, "Right"), DllStructGetData($tRect, "Bottom")))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
