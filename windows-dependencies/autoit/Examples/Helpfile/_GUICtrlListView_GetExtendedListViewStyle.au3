#include <GUIConstantsEx.au3>
#include <GuiListView.au3>
#include <GuiImageList.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hImage, $hListView

	GUICreate("ListView Get Extended List View Style", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
	_GUICtrlListView_SetExtendedListViewStyle($hListView, BitOR($LVS_EX_FULLROWSELECT, $LVS_EX_GRIDLINES, $LVS_EX_SUBITEMIMAGES))
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

	_GUICtrlListView_InsertItem($hListView, "Row 1: Col 1", -1, 0)
	_GUICtrlListView_AddSubItem($hListView, 0, "Row 1: Col 2", 1, 1)
	_GUICtrlListView_AddSubItem($hListView, 0, "Row 1: Col 3", 2, 2)
	_GUICtrlListView_InsertItem($hListView, "Row 2: Col 1", -1, 1)
	_GUICtrlListView_AddSubItem($hListView, 1, "Row 2: Col 2", 1, 2)
	_GUICtrlListView_InsertItem($hListView, "Row 3: Col 1", -1, 2)

	MsgBox(4160, "Information", "Extended List View Style(s): 0x" & _GUICtrlListView_GetExtendedListViewStyle($hListView) & @LF & _
			_DisplayExtendStringList($hListView))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

Func _DisplayExtendStringList($hListView)
	Local $Styles = @LF & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_BORDERSELECT) = $LVS_EX_BORDERSELECT) Then $Styles &= "$LVS_EX_BORDERSELECT" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_CHECKBOXES) = $LVS_EX_CHECKBOXES) Then $Styles &= "$LVS_EX_CHECKBOXES" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_DOUBLEBUFFER) = $LVS_EX_DOUBLEBUFFER) Then $Styles &= "$LVS_EX_DOUBLEBUFFER" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_FLATSB) = $LVS_EX_FLATSB) Then $Styles &= "$LVS_EX_FLATSB" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_FULLROWSELECT) = $LVS_EX_FULLROWSELECT) Then $Styles &= "$LVS_EX_FULLROWSELECT" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_GRIDLINES) = $LVS_EX_GRIDLINES) Then $Styles &= "$LVS_EX_GRIDLINES" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_HEADERDRAGDROP) = $LVS_EX_HEADERDRAGDROP) Then $Styles &= "$LVS_EX_HEADERDRAGDROP" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_INFOTIP) = $LVS_EX_INFOTIP) Then $Styles &= "$LVS_EX_INFOTIP" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_LABELTIP) = $LVS_EX_LABELTIP) Then $Styles &= "$LVS_EX_LABELTIP" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_MULTIWORKAREAS) = $LVS_EX_MULTIWORKAREAS) Then $Styles &= "$LVS_EX_MULTIWORKAREAS" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_ONECLICKACTIVATE) = $LVS_EX_ONECLICKACTIVATE) Then $Styles &= "$LVS_EX_ONECLICKACTIVATE" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_REGIONAL) = $LVS_EX_REGIONAL) Then $Styles &= "$LVS_EX_REGIONAL" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_SIMPLESELECT) = $LVS_EX_SIMPLESELECT) Then $Styles &= "$LVS_EX_SIMPLESELECT" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_SUBITEMIMAGES) = $LVS_EX_SUBITEMIMAGES) Then $Styles &= "$LVS_EX_SUBITEMIMAGES" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_TRACKSELECT) = $LVS_EX_TRACKSELECT) Then $Styles &= "$LVS_EX_TRACKSELECT" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_TWOCLICKACTIVATE) = $LVS_EX_TWOCLICKACTIVATE) Then $Styles &= "$LVS_EX_TWOCLICKACTIVATE" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_UNDERLINECOLD) = $LVS_EX_UNDERLINECOLD) Then $Styles &= "$LVS_EX_UNDERLINECOLD" & @LF & @TAB
	If (BitAND(_GUICtrlListView_GetExtendedListViewStyle($hListView), $LVS_EX_UNDERLINEHOT) = $LVS_EX_UNDERLINEHOT) Then $Styles &= "$LVS_EX_UNDERLINEHOT" & @LF & @TAB
	Return $Styles
EndFunc   ;==>_DisplayExtendStringList
