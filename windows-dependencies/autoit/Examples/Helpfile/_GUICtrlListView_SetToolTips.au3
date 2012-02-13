#include <GUIConstantsEx.au3>
#include <GuiListView.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hToolTip, $hListView

	GUICreate("ListView Set ToolTips", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
;~ 	$hListView = _GUICtrlListView_Create($hGui, "", 2, 2, 394, 268)
	GUISetState()

	; Add columns
	_GUICtrlListView_AddColumn($hListView, "Items", 100)

	; Add items
	_GUICtrlListView_AddItem($hListView, "Item 1")
	_GUICtrlListView_AddItem($hListView, "Item 2")
	_GUICtrlListView_AddItem($hListView, "Item 3")

	; Show tooltip handle
	$hToolTip = _GUICtrlListView_GetToolTips($hListView)
	MsgBox(4160, "Information", "ToolTip Handle: 0x" & Hex($hToolTip))
	Local $hPrevTooltips = _GUICtrlListView_SetToolTips($hListView, $hToolTip)
	MsgBox(4160, "Information", "Previous ToolTip Handle: 0x" & Hex($hPrevTooltips) & @CRLF & _
			"IsPtr = " & IsPtr($hPrevTooltips) & " IsHWnd = " & IsHWnd($hPrevTooltips))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
