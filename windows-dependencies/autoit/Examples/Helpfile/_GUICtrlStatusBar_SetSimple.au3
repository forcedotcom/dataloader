#include <GUIConstantsEx.au3>
#include <GuiStatusBar.au3>
#include <WindowsConstants.au3>

$Debug_SB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

Global $iMemo

_Main()

Func _Main()

	Local $hGUI, $hStatus
	Local $aParts[3] = [75, 150, -1]

	; Create GUI
	$hGUI = GUICreate("StatusBar Set Simple", 400, 300)

	$hStatus = _GUICtrlStatusBar_Create($hGUI)
	_GUICtrlStatusBar_SetParts($hStatus, $aParts)

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 274, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Get/Set simple format
	MemoWrite("Simple mode on .: " & _GUICtrlStatusBar_IsSimple($hStatus))
	_GUICtrlStatusBar_SetSimple($hStatus, True)
	MemoWrite("Simple mode on .: " & _GUICtrlStatusBar_IsSimple($hStatus))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

; Write message to memo
Func MemoWrite($sMessage = "")
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
