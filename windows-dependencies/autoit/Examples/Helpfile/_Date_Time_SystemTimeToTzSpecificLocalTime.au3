#include <GUIConstantsEx.au3>
#include <Date.au3>
#include <WindowsConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $tLocal, $tSystem

	; Create GUI
	GUICreate("Time", 400, 300)
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 296, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Convert system time to local time
	$tSystem = _Date_Time_GetSystemTime()
	$tLocal = _Date_Time_SystemTimeToTzSpecificLocalTime(DllStructGetPtr($tSystem))
	MemoWrite("System time to local time .: " & _Date_Time_SystemTimeToDateTimeStr($tLocal))

	$tLocal = _Date_Time_GetLocalTime()
	$tSystem = _Date_Time_TzSpecificLocalTimeToSystemTime(DllStructGetPtr($tLocal))
	MemoWrite("Local time to system time .: " & _Date_Time_SystemTimeToDateTimeStr($tSystem))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
