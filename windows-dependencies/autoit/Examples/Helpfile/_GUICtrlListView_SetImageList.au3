#include <GUIConstantsEx.au3>
#include <GuiListView.au3>
#include <GuiImageList.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hImage, $hListView

	GUICreate("ListView Set Image List", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
	GUISetState()

	; Load images
	$hImage = _GUIImageList_Create()
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($hListView, 0xFF0000, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($hListView, 0x00FF00, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($hListView, 0x0000FF, 16, 16))
	Local $hPrevImageList = _GUICtrlListView_SetImageList($hListView, $hImage, 1)

	MsgBox(4160, "Information", "Previous Image List Handle: 0x" & Hex($hPrevImageList) & @CRLF & _
			"IsPtr = " & IsPtr($hPrevImageList) & " IsHWnd = " & IsHWnd($hPrevImageList))

	; Add columns
	_GUICtrlListView_AddColumn($hListView, "Column 1", 100)
	_GUICtrlListView_AddColumn($hListView, "Column 2", 100)
	_GUICtrlListView_AddColumn($hListView, "Column 3", 100)

	; Add items
	_GUICtrlListView_AddItem($hListView, "Item 1", 0)
	_GUICtrlListView_AddItem($hListView, "Item 2", 1)
	_GUICtrlListView_AddItem($hListView, "Item 3", 2)

	; Get image list handle
	MsgBox(4160, "Information", "Image List Handle: 0x" & Hex(_GUICtrlListView_GetImageList($hListView, 1)))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
