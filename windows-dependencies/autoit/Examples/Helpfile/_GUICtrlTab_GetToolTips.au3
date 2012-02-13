#include <GUIConstantsEx.au3>
#include <GuiTab.au3>
#include <GuiToolTip.au3>

$Debug_TAB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hGUI, $hTool, $hTab

	; Create GUI
	$hGUI = GUICreate("Tab Control Get Tool Tips", 400, 300)
	$hTab = _GUICtrlTab_Create($hGUI, 2, 2, 396, 296)
	GUISetState()

	; Add tabs
	_GUICtrlTab_InsertItem($hTab, 0, "Tab 1")
	_GUICtrlTab_InsertItem($hTab, 1, "Tab 2")
	_GUICtrlTab_InsertItem($hTab, 2, "Tab 3")

	; Get/Set tooltip
	$hTool = _GUIToolTip_Create($hTab)
	_GUICtrlTab_SetToolTips($hTab, $hTool)

	MsgBox(4160, "Information", "ToolTip handle: 0x" & _GUICtrlTab_GetToolTips($hTab) & @CRLF & _
			"IsPtr = " & IsPtr(_GUICtrlTab_GetToolTips($hTab)) & " IsHwnd = " & IsHWnd(_GUICtrlTab_GetToolTips($hTab)))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
