#include <GUIConstantsEx.au3>
#include <GuiListView.au3>
#include <GuiImageList.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

Example_UDF_Created() ;use UDF built listview

Func Example_UDF_Created()
	Local $GUI, $hImage, $aImage, $hListView
	Local $exStyles = BitOR($LVS_EX_FULLROWSELECT, $LVS_EX_DOUBLEBUFFER)

	$GUI = GUICreate("(UDF Created) ListView Set Background Image", 600, 550)

	;=========================================================================================================
	$hListView = _GUICtrlListView_Create($GUI, "", 2, 2, 596, 500, -1, -1, True) ; Last option Calls CoInitializeEx
	;=========================================================================================================
	_GUICtrlListView_SetExtendedListViewStyle($hListView, $exStyles)
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

;~ 	_GUICtrlListView_SetBkColor ($hListView, $CLR_NONE)
;~ 	_GUICtrlListView_SetTextColor ($hListView, $CLR_NONE)
;~ 	_GUICtrlListView_SetTextBkColor ($hListView, $CLR_NONE)

	; Get the Image
	Local $sURL = "http://www.autoitscript.com/autoit3/files/graphics/autoit9_wall_grey_800x600.jpg"
	Local $sFilePath = @ScriptDir & "\AutoIt.jpg"
	InetGet($sURL, $sFilePath)

	; Set the Background Image
	_GUICtrlListView_SetBkImage($hListView, $sFilePath)
	$aImage = _GUICtrlListView_GetBkImage($hListView)

	GUISetState()

	MsgBox(4160, "Information", "Background Image: " & $aImage[1])

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	;=========================================================================================================
	DllCall('ole32.dll', 'long', 'CoUinitialize') ; Must call for each CoInitializeEx call made
	;=========================================================================================================

	GUIDelete()
	FileDelete($sFilePath)
EndFunc   ;==>Example_UDF_Created
