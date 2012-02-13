; *******************************************************
; Example 1 - Create a Microsoft Word window and open a document file
; *******************************************************
;
#include <Word.au3>

Local $oWordApp = _WordCreate(@ScriptDir & "\Test.doc")

; *******************************************************
; Example 2 - Attempt to attach to an existing word window with the specified document open.
;               Create a new word window and open that document if one does not already exist.
; *******************************************************
;
#include <Word.au3>
$oWordApp = _WordCreate(@ScriptDir & "\Test.doc", 1)
; Check @extended return value to see if attach was successful
If @extended Then
	MsgBox(0, "", "Attached to Existing Window")
Else
	MsgBox(0, "", "Created New Window")
EndIf

; *******************************************************
; Example 3 - Create a word window with a new blank document
; *******************************************************
;
#include <Word.au3>
$oWordApp = _WordCreate()

; *******************************************************
; Example 4 - Create an invisible word window, open a document,
;               append some text, and quit saving changes.
; *******************************************************
;
#include <Word.au3>
$oWordApp = _WordCreate(@ScriptDir & "\Test.doc", 0, 0)
Local $oDoc = _WordDocGetCollection($oWordApp, 0)
$oDoc.Range.insertAfter("This is some text to insert.")
_WordQuit($oWordApp, -1)
