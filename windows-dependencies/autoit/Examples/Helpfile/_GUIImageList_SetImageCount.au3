#include <GUIConstantsEx.au3>
#include <GuiListView.au3>
#include <GuiImageList.au3>
#include <WindowsConstants.au3>

_Main()

Func _Main()
	Local $listview, $hImage
	Local $exStyles = BitOR($LVS_EX_GRIDLINES, $LVS_EX_FULLROWSELECT, $LVS_EX_SUBITEMIMAGES)

	GUICreate("ImageList Set Image Count", 400, 300)
	$listview = GUICtrlCreateListView("", 2, 2, 394, 268, BitOR($LVS_SHOWSELALWAYS, $LVS_NOSORTHEADER, $LVS_REPORT))
	_GUICtrlListView_SetExtendedListViewStyle($listview, $exStyles)
	GUISetState()

	; Load images
	$hImage = _GUIImageList_Create(16, 16, 5, 3)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 110)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 131)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 165)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 168)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 137)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 146)
	_GUICtrlListView_SetImageList($listview, $hImage, 1)

	; Add columns
	_GUICtrlListView_AddColumn($listview, "Column 1", 120)
	_GUICtrlListView_AddColumn($listview, "Column 2", 100)
	_GUICtrlListView_AddColumn($listview, "Column 3", 100)

	; Add items
	_GUICtrlListView_AddItem($listview, "Row 1: Col 1", 0)
	_GUICtrlListView_AddSubItem($listview, 0, "Row 1: Col 2", 1, 1)
	_GUICtrlListView_AddSubItem($listview, 0, "Row 1: Col 3", 2, 2)
	_GUICtrlListView_AddItem($listview, "Row 2: Col 1", 1)
	_GUICtrlListView_AddSubItem($listview, 1, "Row 2: Col 2", 1, 2)
	_GUICtrlListView_AddItem($listview, "Row 3: Col 1", 2)
	_GUICtrlListView_AddItem($listview, "Row 4: Col 1", 3)
	_GUICtrlListView_AddItem($listview, "Row 5: Col 1", 4)
	_GUICtrlListView_AddSubItem($listview, 4, "Row 5: Col 2", 1, 3)
	_GUICtrlListView_AddItem($listview, "Row 6: Col 1", 5)
	_GUICtrlListView_AddSubItem($listview, 5, "Row 6: Col 2", 1, 4)
	_GUICtrlListView_AddSubItem($listview, 5, "Row 6: Col 3", 2, 3)

	GUISetState(@SW_LOCK)
	MsgBox(4096, "Information", "Setting Image Count")
	_GUIImageList_SetImageCount($hImage, 3)
	GUISetState(@SW_UNLOCK)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
