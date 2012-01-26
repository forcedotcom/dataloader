#include <GUIConstantsEx.au3>
#include <Date.au3>
#include <WindowsConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $tFile, $tLocal

	; Create GUI
	GUICreate("Time", 400, 300)
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 296, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Get system time
	Local $tSystem = _Date_Time_GetSystemTime()
	$tFile = _Date_Time_SystemTimeToFileTime(DllStructGetPtr($tSystem))
	;	--------------------------------------
	$tLocal = _Date_Time_FileTimeToLocalFileTime(DllStructGetPtr($tFile))
	MemoWrite("Local file time .: " & _Date_Time_FileTimeToStr($tLocal))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
