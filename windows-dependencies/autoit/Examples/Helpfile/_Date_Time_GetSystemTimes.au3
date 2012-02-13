#include <GUIConstantsEx.au3>
#include <Date.au3>
#include <WindowsConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $aTime

	; Create GUI
	GUICreate("Time", 400, 300)
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 296, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Get system times
	$aTime = _Date_Time_GetSystemTimes()

	MemoWrite("Idle time ...: " & _Date_Time_FileTimeToStr($aTime[0]))
	MemoWrite("System time .: " & _Date_Time_FileTimeToStr($aTime[1]))
	MemoWrite("User time ...: " & _Date_Time_FileTimeToStr($aTime[2]))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
