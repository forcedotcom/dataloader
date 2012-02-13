#include <GUIConstantsEx.au3>
#include <GuiTab.au3>

$Debug_TAB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $bFormat, $hTab

	; Create GUI
	GUICreate("Tab Control Get Unicode Format", 400, 300)
	$hTab = GUICtrlCreateTab(2, 2, 396, 296)
	GUISetState()

	; Add tabs
	_GUICtrlTab_InsertItem($hTab, 0, "Tab 1")
	_GUICtrlTab_InsertItem($hTab, 1, "Tab 2")
	_GUICtrlTab_InsertItem($hTab, 2, "Tab 3")

	; Get/Set Unicode format
	$bFormat = _GUICtrlTab_GetUnicodeFormat($hTab)
	MsgBox(4160, "Information", "Unicode format: " & $bFormat)
	_GUICtrlTab_SetUnicodeFormat($hTab, Not $bFormat)
	MsgBox(4160, "Information", "Unicode format: " & _GUICtrlTab_GetUnicodeFormat($hTab))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
