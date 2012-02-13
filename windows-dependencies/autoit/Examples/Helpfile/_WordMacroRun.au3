; *******************************************************
; Example 1 - Create a word window, open a document,
;               run a macro named "My Macro" with one
;				argument "Test", quit without saving changes.
; *******************************************************
;
#include <Word.au3>

Local $oWordApp = _WordCreate(@ScriptDir & "\Test.doc")
_WordMacroRun($oWordApp, "My Macro", "Test")
_WordQuit($oWordApp, 0)
