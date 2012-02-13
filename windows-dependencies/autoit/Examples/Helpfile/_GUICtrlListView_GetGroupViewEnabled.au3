#include <GUIConstantsEx.au3>
#include <GuiListView.au3>
#include <GuiImageList.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hImage, $hListView

	GUICreate("ListView Get Group View Enabled", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
	_GUICtrlListView_SetExtendedListViewStyle($hListView, $LVS_EX_FULLROWSELECT)
	GUISetState()

	; Load images
	$hImage = _GUIImageList_Create()
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap(GUICtrlGetHandle($hListView), 0xFF0000, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap(GUICtrlGetHandle($hListView), 0x00FF00, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap(GUICtrlGetHandle($hListView), 0x0000FF, 16, 16))
	_GUICtrlListView_SetImageList($hListView, $hImage, 1)

	; Add columns
	_GUICtrlListView_AddColumn($hListView, "Column 1", 100)
	_GUICtrlListView_AddColumn($hListView, "Column 2", 100)
	_GUICtrlListView_AddColumn($hListView, "Column 3", 100)

	; Add items
	_GUICtrlListView_AddItem($hListView, "Row 1: Col 1", 0)
	_GUICtrlListView_AddSubItem($hListView, 0, "Row 1: Col 2", 1)
	_GUICtrlListView_AddSubItem($hListView, 0, "Row 1: Col 3", 2)
	_GUICtrlListView_AddItem($hListView, "Row 2: Col 1", 1)
	_GUICtrlListView_AddSubItem($hListView, 1, "Row 2: Col 2", 1)
	_GUICtrlListView_AddItem($hListView, "Row 3: Col 1", 2)

	; Build groups
	_GUICtrlListView_EnableGroupView($hListView)
	_GUICtrlListView_InsertGroup($hListView, -1, 1, "Group 1")
	_GUICtrlListView_InsertGroup($hListView, -1, 2, "Group 2")
	_GUICtrlListView_SetItemGroupID($hListView, 0, 1)
	_GUICtrlListView_SetItemGroupID($hListView, 1, 2)
	_GUICtrlListView_SetItemGroupID($hListView, 2, 2)

	; Is group view enabled
	MsgBox(4160, "Information", "Group View Enabled: " & _GUICtrlListView_GetGroupViewEnabled($hListView))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
