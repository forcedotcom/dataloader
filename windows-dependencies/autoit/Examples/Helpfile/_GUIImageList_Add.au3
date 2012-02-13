#include <GUIConstantsEx.au3>
#include <WinAPI.au3>
#include <GuiListView.au3>
#include <GuiImageList.au3>
#include <WindowsConstants.au3>

_Main()

Func _Main()
	Local $listview, $hImage
	Local $exStyles = BitOR($LVS_EX_GRIDLINES, $LVS_EX_FULLROWSELECT, $LVS_EX_SUBITEMIMAGES)

	GUICreate("ImageList Add", 400, 300)
	$listview = GUICtrlCreateListView("", 2, 2, 394, 268, BitOR($LVS_SHOWSELALWAYS, $LVS_NOSORTHEADER, $LVS_REPORT))
	_GUICtrlListView_SetExtendedListViewStyle($listview, $exStyles)
	GUISetState()

	; Load images
	$hImage = _GUIImageList_Create(11, 11)
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($listview, 0xFF0000, 11, 11))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($listview, 0x00FF00, 11, 11))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($listview, 0x0000FF, 11, 11))
	_GUICtrlListView_SetImageList($listview, $hImage, 1)

	; Add columns
	_GUICtrlListView_AddColumn($listview, "Items", 120)

	; Add items
	_GUICtrlListView_AddItem($listview, "Item 1", 0)
	_GUICtrlListView_AddItem($listview, "Item 2", 1)
	_GUICtrlListView_AddItem($listview, "Item 3", 2)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
