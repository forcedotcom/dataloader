; *******************************************************
; Example 1 - Create a word window, open a document, check to see if the
;               statusbar is visible, if it is turn it off, if it isn't turn it on.
; *******************************************************
;
#include <Word.au3>

Local $oWordApp = _WordCreate(@ScriptDir & "\Test.doc")
If _WordPropertyGet($oWordApp, "statusbar") Then
	MsgBox(0, "StatusBar Status", "StatusBar Visible, turning it off.")
	_WordPropertySet($oWordApp, "statusbar", False)
Else
	MsgBox(0, "StatusBar Status", "StatusBar Invisible, turning it on.")
	_WordPropertySet($oWordApp, "statusbar", True)
EndIf
