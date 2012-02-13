#include <GUIConstantsEx.au3>
#include <GuiDateTimePicker.au3>

$Debug_DTP = False ; Check ClassName being passed to DTP functions, set to True and use a handle to another control to see it work

Global $iMemo, $tRange, $aDate

_Main()

Func _Main()
	Local $hDTP, $a_Date[7] = [False, @YEAR, 8, 19, 21, 57, 34]

	; Create GUI
	GUICreate("DateTimePick Set System Time", 400, 300)
	$hDTP = GUICtrlGetHandle(GUICtrlCreateDate("", 2, 6, 190))
	$iMemo = GUICtrlCreateEdit("", 2, 32, 396, 266, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Set the display format
	_GUICtrlDTP_SetFormat($hDTP, "ddd MMM dd, yyyy hh:mm ttt")

	; Set system time
	_GUICtrlDTP_SetSystemTime($hDTP, $a_Date)

	; Display system time
	$aDate = _GUICtrlDTP_GetSystemTime($hDTP)
	MemoWrite("Current date: " & GetDateStr())
	MemoWrite("Current time: " & GetTimeStr())

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

; Returns the date portion
Func GetDateStr()
	Return StringFormat("%02d/%02d/%04d", $aDate[1], $aDate[2], $aDate[0])
EndFunc   ;==>GetDateStr

; Returns the time portion
Func GetTimeStr()
	Return StringFormat("%02d:%02d:%02d", $aDate[3], $aDate[4], $aDate[5])
EndFunc   ;==>GetTimeStr

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
