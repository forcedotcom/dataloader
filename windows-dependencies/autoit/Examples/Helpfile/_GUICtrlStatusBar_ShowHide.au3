#include <GUIConstantsEx.au3>
#include <GuiStatusBar.au3>

$Debug_SB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()

	Local $hGUI, $hStatus
	Local $aParts[3] = [75, 150, -1]

	; Create GUI
	$hGUI = GUICreate("StatusBar Show/Hide", 400, 300)

	;===============================================================================
	; defaults to 1 part, no text
	$hStatus = _GUICtrlStatusBar_Create($hGUI)
	;===============================================================================
	_GUICtrlStatusBar_SetParts($hStatus, $aParts)

	GUISetState()

	MsgBox(4160, "Information", "Hide StatusBar")
	_GUICtrlStatusBar_ShowHide($hStatus, @SW_HIDE)
	MsgBox(4160, "Information", "Show StatusBar")
	_GUICtrlStatusBar_ShowHide($hStatus, @SW_SHOW)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
