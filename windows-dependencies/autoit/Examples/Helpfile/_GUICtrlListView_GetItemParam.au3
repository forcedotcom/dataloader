#include <GUIConstantsEx.au3>
#include <GuiListView.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

; Warning do not use SetItemParam on items created with GuiCtrlCreateListViewItem
; Param is the controlId for items created with built-in function

Example_UDF_Created()

Func Example_UDF_Created()
	Local $GUI, $hListView

	$GUI = GUICreate("(UDF Created) ListView Get Item Param", 400, 300)
	$hListView = _GUICtrlListView_Create($GUI, "", 2, 2, 394, 268)
	GUISetState()

	; Add columns
	_GUICtrlListView_AddColumn($hListView, "Items", 100)

	; Add items
	_GUICtrlListView_AddItem($hListView, "Item 1")
	_GUICtrlListView_AddItem($hListView, "Item 2")
	_GUICtrlListView_AddItem($hListView, "Item 3")

	; Set item 2 parameter
	_GUICtrlListView_SetItemParam($hListView, 1, 1234)
	MsgBox(4160, "Information", "Item 2 Parameter: " & _GUICtrlListView_GetItemParam($hListView, 1))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>Example_UDF_Created
