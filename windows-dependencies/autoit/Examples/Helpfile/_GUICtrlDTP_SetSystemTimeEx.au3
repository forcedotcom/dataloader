#include <GUIConstantsEx.au3>
#include <GuiDateTimePicker.au3>

$Debug_DTP = False ; Check ClassName being passed to DTP functions, set to True and use a handle to another control to see it work

Global $iMemo, $tDate

_Main()

Func _Main()
	Local $hDTP

	; Create GUI
	GUICreate("DateTimePick Set System TimeEx", 400, 300)
	$hDTP = GUICtrlGetHandle(GUICtrlCreateDate("", 2, 6, 190))
	$iMemo = GUICtrlCreateEdit("", 2, 32, 396, 266, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Set the display format
	_GUICtrlDTP_SetFormat($hDTP, "ddd MMM dd, yyyy hh:mm ttt")

	; Set system time
	$tDate = DllStructCreate($tagSYSTEMTIME)
	DllStructSetData($tDate, "Year", @YEAR)
	DllStructSetData($tDate, "Month", 8)
	DllStructSetData($tDate, "Day", 19)
	DllStructSetData($tDate, "Hour", 21)
	DllStructSetData($tDate, "Minute", 57)
	DllStructSetData($tDate, "Second", 34)
	_GUICtrlDTP_SetSystemTimeEx($hDTP, $tDate)

	; Display system time
	$tDate = _GUICtrlDTP_GetSystemTimeEx($hDTP)
	MemoWrite("Current date: " & GetDateStr())
	MemoWrite("Current time: " & GetTimeStr())

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

; Returns the date portion
Func GetDateStr()
	Return StringFormat("%02d/%02d/%04d", DllStructGetData($tDate, "Month"), DllStructGetData($tDate, "Day"), DllStructGetData($tDate, "Year"))
EndFunc   ;==>GetDateStr

; Returns the time portion
Func GetTimeStr()
	Return StringFormat("%02d:%02d:%02d", DllStructGetData($tDate, "Hour"), DllStructGetData($tDate, "Minute"), DllStructGetData($tDate, "Second"))
EndFunc   ;==>GetTimeStr

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
