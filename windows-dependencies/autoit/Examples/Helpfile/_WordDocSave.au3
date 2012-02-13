; *******************************************************
; Example 1 - Open an existing word document, append some text,
;				save and quit.
; *******************************************************
;
#include <Word.au3>

Local $oWordApp = _WordCreate(@ScriptDir & "\Test.doc")
Local $oDoc = _WordDocGetCollection($oWordApp, 0)
$oDoc.Range.insertAfter("This is some text to insert.")
_WordDocSave($oDoc)
_WordQuit($oWordApp)
