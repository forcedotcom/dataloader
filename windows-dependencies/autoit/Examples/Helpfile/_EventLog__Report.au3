#include <EventLog.au3>

_Main()

Func _Main()
	Local $hEventLog, $aData[4] = [3, 1, 2, 3]

	$hEventLog = _EventLog__Open("", "Application")
	_EventLog__Report($hEventLog, 4, 0, 2, "Administrator", "AutoIt3 generated event", $aData)
	_EventLog__Close($hEventLog)

EndFunc   ;==>_Main
