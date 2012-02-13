#include <GUIConstantsEx.au3>
#include <GuiListView.au3>
#include <GuiImageList.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

Example_UDF_Created()

Func Example_UDF_Created()
	Local $GUI, $hImage, $hListView

	$GUI = GUICreate("(UDF Created) ListView Get CallBack Mask", 400, 300)

	$hListView = _GUICtrlListView_Create($GUI, "", 2, 2, 394, 268)
	GUISetState()

	_GUICtrlListView_SetCallBackMask($hListView, 32)
	MsgBox(4160, "Information", "CallBackMask: " & _GUICtrlListView_GetCallbackMask($hListView))

	; Load images
	$hImage = _GUIImageList_Create()
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($hListView, 0xFF0000, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($hListView, 0x00FF00, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($hListView, 0x0000FF, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($hListView, 0xC0C0C0, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($hListView, 0xFF00FF, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlListView_CreateSolidBitMap($hListView, 0xFFFF00, 16, 16))
	_GUICtrlListView_SetImageList($hListView, $hImage, 1)
	_GUICtrlListView_SetImageList($hListView, $hImage, 2)

	; Add columns
	_GUICtrlListView_AddColumn($hListView, "Column 1", 100)
	_GUICtrlListView_AddColumn($hListView, "Column 2", 100)
	_GUICtrlListView_AddColumn($hListView, "Column 3", 100)

	; Add items with callback for item text
	_GUICtrlListView_AddItem($hListView, -1, 0)
	_GUICtrlListView_AddItem($hListView, -1, 1)
	_GUICtrlListView_AddItem($hListView, -1, 2)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>Example_UDF_Created
