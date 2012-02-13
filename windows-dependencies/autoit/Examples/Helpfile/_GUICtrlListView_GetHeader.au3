#include <GUIConstantsEx.au3>
#include <GuiListView.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hListView

	GUICreate("ListView Get Header", 400, 300)
	$hListView = GUICtrlCreateListView("col1|col2|col3", 2, 2, 394, 268)
	GUISetState()

	MsgBox(4160, "Information", "ListView Header Handle: 0x" & Hex(_GUICtrlListView_GetHeader($hListView)) & @CRLF & _
			"IsPtr = " & IsPtr(_GUICtrlListView_GetHeader($hListView)) & " IsHwnd = " & IsHWnd(_GUICtrlListView_GetHeader($hListView)))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

	GUIDelete()
EndFunc   ;==>_Main
