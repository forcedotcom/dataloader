#include <GUIConstantsEx.au3>
#include <GuiListView.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

Example1()
Example2()
Example_UDF_Created()

Func Example1()
	Local $hListView

	GUICreate("ListView Delete All Items", 400, 300)

	$hListView = GUICtrlCreateListView("col1|col2|col3", 2, 2, 394, 268)
	GUISetState()

	; 3 column load
	For $iI = 0 To 9
		GUICtrlCreateListViewItem("Item " & $iI & "|Item " & $iI & "-1|Item " & $iI & "-2", $hListView)
	Next

	MsgBox(4160, "Information", "Delete All Items")
	; Items created using built-in function, pass the control ID
	MsgBox(4160, "Deleted?", _GUICtrlListView_DeleteAllItems($hListView))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>Example1

Func Example2()
	Local $hListView, $aItems[10][3]

	GUICreate("ListView Delete All Items", 400, 300)

	$hListView = GUICtrlCreateListView("col1|col2|col3", 2, 2, 394, 268)
	GUISetState()

	; 3 column load
	For $iI = 0 To UBound($aItems) - 1
		$aItems[$iI][0] = "Item " & $iI
		$aItems[$iI][1] = "Item " & $iI & "-1"
		$aItems[$iI][2] = "Item " & $iI & "-2"
	Next

	_GUICtrlListView_AddArray($hListView, $aItems)

	MsgBox(4160, "Information", "Delete All Items")
	; Items created using UDF function(s), pass the handle to the control
	MsgBox(4160, "Deleted?", _GUICtrlListView_DeleteAllItems(GUICtrlGetHandle($hListView)))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>Example2

Func Example_UDF_Created()
	Local $GUI, $hListView, $aItems[10][3]

	$GUI = GUICreate("(UDF Created) ListView Delete All Items", 400, 300)

	$hListView = _GUICtrlListView_Create($GUI, "col1|col2|col3", 2, 2, 394, 268)
	GUISetState()

	; 3 column load
	For $iI = 0 To UBound($aItems) - 1
		$aItems[$iI][0] = "Item " & $iI
		$aItems[$iI][1] = "Item " & $iI & "-1"
		$aItems[$iI][2] = "Item " & $iI & "-2"
	Next

	_GUICtrlListView_AddArray($hListView, $aItems)

	MsgBox(4160, "Information", "Delete All Items")
	; This is already a handle
	MsgBox(4160, "Deleted?", _GUICtrlListView_DeleteAllItems($hListView))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>Example_UDF_Created
