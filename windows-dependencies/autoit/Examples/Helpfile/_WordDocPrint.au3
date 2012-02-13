; *******************************************************
; Example 1 - Create a word window, open a document, set the text,
;				print with defaults, and quit without saving changes.
; *******************************************************
;
#include <Word.au3>

Local $oWordApp = _WordCreate(@ScriptDir & "\Test.doc")
Local $oDoc = _WordDocGetCollection($oWordApp, 0)
$oDoc.Range.Text = "This is some text to print."
_WordDocPrint($oDoc)
_WordQuit($oWordApp, 0)

; *******************************************************
; Example 2 - Create a word window, open a document, set the text,
;				print using landscape, and quit without saving changes.
; *******************************************************
;
#include <Word.au3>
$oWordApp = _WordCreate(@ScriptDir & "\Test.doc")
$oDoc = _WordDocGetCollection($oWordApp, 0)
$oDoc.Range.Text = "This is some text to print."
_WordDocPrint($oDoc, 0, 1, 1)
_WordQuit($oWordApp, 0)

; *******************************************************
; Example 3 - Create a word window, open a document, set the text,
;				print to a printer named "My Printer", and quit without saving changes.
; *******************************************************
;
#include <Word.au3>
$oWordApp = _WordCreate(@ScriptDir & "\Test.doc")
$oDoc = _WordDocGetCollection($oWordApp, 0)
$oDoc.Range.Text = "This is some text to print."
_WordDocPrint($oDoc, 0, 1, 0, 1, "My Printer")
_WordQuit($oWordApp, 0)
