; *******************************************************
; Example 1 - Check the current status of _IEErrorNotify, turn it off if on, on if off
; *******************************************************

#include <IE.au3>

If _IEErrorNotify() Then
	MsgBox(0, "_IEErrorNotify Status", "Notification is ON, turning it OFF")
	_IEErrorNotify(False)
Else
	MsgBox(0, "_IEErrorNotify Status", "Notification is OFF, turning it ON")
	_IEErrorNotify(True)
EndIf
