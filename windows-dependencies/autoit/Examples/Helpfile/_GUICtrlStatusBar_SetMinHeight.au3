#include <GUIConstantsEx.au3>
#include <GuiStatusBar.au3>
#include <WinAPI.au3>

$Debug_SB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()

	Local $hGUI, $hStatus
	Local $aParts[3] = [75, 150, -1]

	; Create GUI
	$hGUI = GUICreate("(Example 1) StatusBar Set Min Height", 400, 300)
	$hStatus = _GUICtrlStatusBar_Create($hGUI)
	GUISetState()

	; Set parts
	_GUICtrlStatusBar_SetParts($hStatus, $aParts)
	_GUICtrlStatusBar_SetText($hStatus, "Part 1")
	_GUICtrlStatusBar_SetText($hStatus, "Part 2", 1)

	; Set minimum height
	_GUICtrlStatusBar_SetMinHeight($hStatus, 30)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
