#include <GUIConstantsEx.au3>
#include <WinAPI.au3>
#include <GuiListView.au3>
#include <GuiImageList.au3>
#include <WindowsConstants.au3>

_Main()

Func _Main()
	Local $listview, $hImage
	Local $Wow64 = ""
	If @AutoItX64 Then $Wow64 = "\Wow6432Node"
	Local $sPath = RegRead("HKEY_LOCAL_MACHINE\SOFTWARE" & $Wow64 & "\AutoIt v3\AutoIt", "InstallDir") & "\Examples\GUI\Advanced\Images"

	GUICreate("ImageList AddBitmap", 400, 300)
	$listview = GUICtrlCreateListView("", 2, 2, 394, 268, BitOR($LVS_SHOWSELALWAYS, $LVS_NOSORTHEADER, $LVS_REPORT))
	_GUICtrlListView_SetExtendedListViewStyle($listview, BitOR($LVS_EX_FULLROWSELECT, $LVS_EX_GRIDLINES, $LVS_EX_DOUBLEBUFFER))
	GUISetState()

	; Load images
	$hImage = _GUIImageList_Create(16, 32)
	_GUIImageList_AddBitmap($hImage, $sPath & "\Red.bmp")
	_GUIImageList_AddBitmap($hImage, $sPath & "\Green.bmp")
	_GUIImageList_AddBitmap($hImage, $sPath & "\Blue.bmp")
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
