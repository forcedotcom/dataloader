; *******************************************************
; Example 1 - Check the current status of _WordErrorNotify, turn it off if on, on if off
; *******************************************************
;
#include <Word.au3>

If _WordErrorNotify() Then
	MsgBox(0, "_WordErrorNotify Status", "Notification is ON, turning it OFF")
	_WordErrorNotify(1)
Else
	MsgBox(0, "_WordErrorNotify Status", "Notification is OFF, turning it ON")
	_WordErrorNotify(0)
EndIf
