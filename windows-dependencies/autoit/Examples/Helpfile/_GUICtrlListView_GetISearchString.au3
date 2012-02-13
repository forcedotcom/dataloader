#include <GUIConstantsEx.au3>
#include <GuiListView.au3>
#include <GuiImageList.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hImage, $hListView

	GUICreate("ListView Get ISearch", 400, 300)

	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
	GUICtrlSetStyle($hListView, $LVS_ICON)
	GUISetState()

	; Load images
	$hImage = _GUIImageList_Create()
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($hListView, 0xFF0000, 16, 16))
	_GUICtrlListView_SetImageList($hListView, $hImage, 0)

	_GUICtrlListView_BeginUpdate($hListView)
	For $x = 1 To 10
		_GUICtrlListView_InsertItem($hListView, "Item " & $x, -1, 0)
	Next
	_GUICtrlListView_EndUpdate($hListView)

	Send("Item 1")

	; Get incremental search string
	MsgBox(4160, "Information", "Incremental Search String: " & _GUICtrlListView_GetISearchString($hListView))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

	GUIDelete()
EndFunc   ;==>_Main
