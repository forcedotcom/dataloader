#include <GUIConstantsEx.au3>
#include <GuiTab.au3>

$Debug_TAB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

; Warning do not use SetItemParam on items created with GUICtrlCreateTabItem
; Param is the controlId for items created with built-in functions

_Main()

Func _Main()
	Local $hGUI, $hTab

	; Create GUI
	$hGUI = GUICreate("(UDF Created) Tab Control Set Item Param", 400, 300)
	$hTab = _GUICtrlTab_Create($hGUI, 2, 2, 396, 296)
	GUISetState()

	; Add tabs
	_GUICtrlTab_InsertItem($hTab, 0, "Tab 1")
	_GUICtrlTab_InsertItem($hTab, 1, "Tab 2")
	_GUICtrlTab_InsertItem($hTab, 2, "Tab 3")

	; Get/Set tab 1 parameter
	_GUICtrlTab_SetItemParam($hTab, 0, 1234)
	MsgBox(4160, "Information", "Tab 1 parameter: " & _GUICtrlTab_GetItemParam($hTab, 0))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
