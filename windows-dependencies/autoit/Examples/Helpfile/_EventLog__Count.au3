#include <GUIConstantsEx.au3>
#include <EventLog.au3>

Global $iMemo

_Main()

Func _Main()
	Local $hEventLog

	; Create GUI
	GUICreate("EventLog", 400, 300)
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 300, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	$hEventLog = _EventLog__Open("", "Application")
	MemoWrite("Log full ........: " & _EventLog__Full($hEventLog))
	MemoWrite("Log record count : " & _EventLog__Count($hEventLog))
	MemoWrite("Log oldest record: " & _EventLog__Oldest($hEventLog))
	_EventLog__Close($hEventLog)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
