#include <GUIConstantsEx.au3>
#include <GuiTab.au3>

$Debug_TAB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hTab

	; Create GUI
	GUICreate("Tab Control Get Row Count", 400, 300)
	$hTab = GUICtrlCreateTab(2, 2, 396, 296, $TCS_MULTILINE)
	GUISetState()

	; Add tabs
	For $x = 0 To 10
		_GUICtrlTab_InsertItem($hTab, $x, "Tab " & $x + 1)
	Next

	; Get row count
	MsgBox(4160, "Information", "Row count: " & _GUICtrlTab_GetRowCount($hTab))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
