; *******************************************************
; Example 1 - Create an empty word window and open an existing document
; *******************************************************
;
#include <Word.au3>

Local $oWordApp = _WordCreate("")
Local $oDoc = _WordDocOpen($oWordApp, @ScriptDir & "\Test.doc")
