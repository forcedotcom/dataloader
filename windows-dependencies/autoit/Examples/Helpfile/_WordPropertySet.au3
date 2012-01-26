; *******************************************************
; Example 1 - Create a word window, open a document,
;				hide the window, then make it visible again.
; *******************************************************
;
#include <Word.au3>

Local $oWordApp = _WordCreate(@ScriptDir & "\Test.doc")
Sleep(2000)
_WordPropertySet($oWordApp, "visible", False)
Sleep(2000)
_WordPropertySet($oWordApp, "visible", True)
