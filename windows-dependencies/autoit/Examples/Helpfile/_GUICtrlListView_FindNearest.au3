#include <GUIConstantsEx.au3>
#include <GuiListView.au3>
#include <GuiImageList.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Global $hImage, $iIndex, $hListView

	; Create GUI
	GUICreate("ListView Find Nearest", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
	GUICtrlSetStyle($hListView, $LVS_ICON)
	GUISetState()

	; Load images
	$hImage = _GUIImageList_Create()
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap(GUICtrlGetHandle($hListView), 0xFF0000, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap(GUICtrlGetHandle($hListView), 0x00FF00, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap(GUICtrlGetHandle($hListView), 0x0000FF, 16, 16))
	_GUICtrlListView_SetImageList($hListView, $hImage, 0)

	; Add items
	_GUICtrlListView_AddItem($hListView, "Item 1", 0)
	_GUICtrlListView_AddItem($hListView, "Item 2", 1)
	_GUICtrlListView_AddItem($hListView, "Item 3", 2)

	; Find nearest items
	$iIndex = _GUICtrlListView_FindNearest($hListView, 100, 10)
	MsgBox(4160, "Information", "Item nearest [100, 10]: " & $iIndex)

	$iIndex = _GUICtrlListView_FindNearest($hListView, 200, 10)
	MsgBox(4160, "Information", "Item nearest [200, 10]: " & $iIndex)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
