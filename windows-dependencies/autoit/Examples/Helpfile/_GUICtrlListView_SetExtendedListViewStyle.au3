#include <GUIConstantsEx.au3>
#include <GuiListView.au3>
#include <GuiImageList.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hImage, $hListView

	; Create GUI
	GUICreate("ListView Set Extended Style", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
	_GUICtrlListView_SetExtendedListViewStyle($hListView, BitOR($LVS_EX_FULLROWSELECT, $LVS_EX_SUBITEMIMAGES))
	GUISetState()

	; Load images
	$hImage = _GUIImageList_Create()
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($hListView, 0xFF0000, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($hListView, 0x00FF00, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($hListView, 0x0000FF, 16, 16))
	_GUICtrlListView_SetImageList($hListView, $hImage, 1)

	; Add columns
	_GUICtrlListView_InsertColumn($hListView, 0, "Column 1", 100)
	_GUICtrlListView_InsertColumn($hListView, 1, "Column 2", 100)
	_GUICtrlListView_InsertColumn($hListView, 2, "Column 3", 100)

	; Add items
	_GUICtrlListView_AddItem($hListView, "Row 1: Col 1", 0)
	_GUICtrlListView_AddSubItem($hListView, 0, "Row 1: Col 2", 1, 1)
	_GUICtrlListView_AddSubItem($hListView, 0, "Row 1: Col 3", 2, 2)
	_GUICtrlListView_AddItem($hListView, "Row 2: Col 1", 1)
	_GUICtrlListView_AddSubItem($hListView, 1, "Row 2: Col 2", 1, 2)
	_GUICtrlListView_AddItem($hListView, "Row 3: Col 1", 2)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
