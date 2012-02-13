#include <GUIConstantsEx.au3>
#include <GuiTab.au3>

$Debug_TAB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $aRect, $sRect, $hTab

	; Create GUI
	GUICreate("Tab Control Get Display Rect", 400, 300)
	$hTab = GUICtrlCreateTab(2, 2, 396, 296)
	GUISetState()

	; Add tabs
	_GUICtrlTab_InsertItem($hTab, 0, "Tab 1")
	_GUICtrlTab_InsertItem($hTab, 1, "Tab 2")
	_GUICtrlTab_InsertItem($hTab, 2, "Tab 3")

	; Get display rectangle
	$aRect = _GUICtrlTab_GetDisplayRect(GUICtrlGetHandle($hTab))
	$sRect = StringFormat("[%d, %d, %d, %d]", $aRect[0], $aRect[1], $aRect[2], $aRect[3])
	MsgBox(4160, "Information", "Display rectangle: " & $sRect)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
