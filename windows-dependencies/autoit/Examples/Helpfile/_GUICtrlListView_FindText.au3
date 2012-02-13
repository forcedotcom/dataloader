#include <GUIConstantsEx.au3>
#include <GuiListView.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Global $iI, $hListView

	; Create GUI
	GUICreate("ListView Find Text", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
	GUISetState()

	; Add columns
	_GUICtrlListView_AddColumn($hListView, "Items", 100)

	; Add items
	_GUICtrlListView_BeginUpdate($hListView)
	For $iI = 1 To 49
		_GUICtrlListView_AddItem($hListView, "Item " & $iI)
	Next
	_GUICtrlListView_AddItem($hListView, "Target item")
	For $iI = 51 To 100
		_GUICtrlListView_AddItem($hListView, "Item " & $iI)
	Next
	_GUICtrlListView_EndUpdate($hListView)

	; Search for target item
	$iI = _GUICtrlListView_FindText($hListView, "tArGeT")
	MsgBox(4160, "Information", "Target Item Index: " & $iI)
	_GUICtrlListView_EnsureVisible($hListView, $iI)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
