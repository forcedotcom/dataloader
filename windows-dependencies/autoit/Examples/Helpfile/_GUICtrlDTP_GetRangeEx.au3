#include <GUIConstantsEx.au3>
#include <GuiDateTimePicker.au3>

$Debug_DTP = False ; Check ClassName being passed to DTP functions, set to True and use a handle to another control to see it work

Global $iMemo, $tRange

_Main()

Func _Main()
	Local $hDTP

	; Create GUI
	GUICreate("DateTimePick Get RangeEx", 400, 300)
	$hDTP = GUICtrlGetHandle(GUICtrlCreateDate("", 2, 6, 190))
	$iMemo = GUICtrlCreateEdit("", 2, 32, 396, 266, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Set the display format
	_GUICtrlDTP_SetFormat($hDTP, "ddd MMM dd, yyyy hh:mm ttt")

	; Set date range
	$tRange = DllStructCreate($tagDTPRANGE)
	DllStructSetData($tRange, "MinValid", True)
	DllStructSetData($tRange, "MinYear", @YEAR)
	DllStructSetData($tRange, "MinMonth", 1)
	DllStructSetData($tRange, "MinDay", 1)
	DllStructSetData($tRange, "MinHour", 0)
	DllStructSetData($tRange, "MinMinute", 0)
	DllStructSetData($tRange, "MinSecond", 1)
	DllStructSetData($tRange, "MaxValid", True)
	DllStructSetData($tRange, "MaxYear", @YEAR)
	DllStructSetData($tRange, "MaxMonth", 12)
	DllStructSetData($tRange, "MaxDay", 31)
	DllStructSetData($tRange, "MaxHour", 12)
	DllStructSetData($tRange, "MaxMinute", 59)
	DllStructSetData($tRange, "MaxSecond", 59)
	_GUICtrlDTP_SetRangeEx($hDTP, $tRange)

	; Display date range
	$tRange = _GUICtrlDTP_GetRangeEx($hDTP)
	MemoWrite("Minimum date: " & GetDateStr("Min"))
	MemoWrite("Maximum date: " & GetDateStr("Max"))
	MemoWrite("Minimum time: " & GetTimeStr("Min"))
	MemoWrite("Maximum time: " & GetTimeStr("Max"))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

; Returns the date portion
Func GetDateStr($sPrefix)
	If $sPrefix = "Min" Then
		Return StringFormat("%02d/%02d/%04d", DllStructGetData($tRange, "MinMonth"), DllStructGetData($tRange, "MinDay"), DllStructGetData($tRange, "MinYear"))
	Else
		Return StringFormat("%02d/%02d/%04d", DllStructGetData($tRange, "MaxMonth"), DllStructGetData($tRange, "MaxDay"), DllStructGetData($tRange, "MaxYear"))
	EndIf
EndFunc   ;==>GetDateStr

; Returns the time portion
Func GetTimeStr($sPrefix)
	If $sPrefix = "Min" Then
		Return StringFormat("%02d:%02d:%02d", DllStructGetData($tRange, "MinHour"), DllStructGetData($tRange, "MinMinute"), DllStructGetData($tRange, "MinSecond"))
	Else
		Return StringFormat("%02d:%02d:%02d", DllStructGetData($tRange, "MaxHour"), DllStructGetData($tRange, "MaxMinute"), DllStructGetData($tRange, "MaxSecond"))
	EndIf
EndFunc   ;==>GetTimeStr

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
