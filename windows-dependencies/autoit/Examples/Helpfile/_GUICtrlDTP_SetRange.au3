#include <GUIConstantsEx.au3>
#include <GuiDateTimePicker.au3>

$Debug_DTP = False ; Check ClassName being passed to DTP functions, set to True and use a handle to another control to see it work

Global $iMemo, $aRange[14] = [True, @YEAR, 1, 1, 21, 45, 32, True, @YEAR, 12, 31, 23, 59, 59]

_Main()

Func _Main()
	Local $hDTP

	; Create GUI
	GUICreate("DateTimePick Set Range", 400, 300)
	$hDTP = GUICtrlGetHandle(GUICtrlCreateDate("", 2, 6, 190))
	$iMemo = GUICtrlCreateEdit("", 2, 32, 396, 266, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Set the display format
	_GUICtrlDTP_SetFormat($hDTP, "ddd MMM dd, yyyy hh:mm ttt")

	; Set date range
	_GUICtrlDTP_SetRange($hDTP, $aRange)

	; Display date range
	$aRange = _GUICtrlDTP_GetRange($hDTP)
	MemoWrite("Minimum date: " & GetDateStr(0))
	MemoWrite("Maximum date: " & GetDateStr(7))
	MemoWrite("Minimum time: " & GetTimeStr(4))
	MemoWrite("Maximum time: " & GetTimeStr(11))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

; Returns the date portion
Func GetDateStr($iOff = 0)
	Return StringFormat("%02d/%02d/%04d", $aRange[$iOff + 2], $aRange[$iOff + 3], $aRange[$iOff + 1])
EndFunc   ;==>GetDateStr

; Returns the time portion
Func GetTimeStr($iOff = 0)
	Return StringFormat("%02d:%02d:%02d", $aRange[$iOff], $aRange[$iOff + 1], $aRange[$iOff + 2])
EndFunc   ;==>GetTimeStr

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
