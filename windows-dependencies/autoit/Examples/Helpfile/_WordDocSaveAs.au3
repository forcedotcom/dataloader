; *******************************************************
; Example 1 - Create a word window with a new blank document, add some text,
;				execute a saveas operation, then quit.
; *******************************************************
;
#include <Word.au3>

Local $oWordApp = _WordCreate()
Local $oDoc = _WordDocGetCollection($oWordApp, 0)
$oDoc.Range.Text = "This is some text to insert."
_WordDocSaveAs($oDoc, @ScriptDir & "\Test.doc")
_WordQuit($oWordApp)
