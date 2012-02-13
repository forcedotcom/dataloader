; *******************************************************
; Example 1 - Create an empty word window, open an existing document,
;				close the document and quit.
; *******************************************************
;
#include <Word.au3>

Local $oWordApp = _WordCreate("")
Local $oDoc = _WordDocOpen($oWordApp, @ScriptDir & "\Test.doc")
_WordDocClose($oDoc)
_WordQuit($oWordApp)
