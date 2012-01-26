; *******************************************************
; Example 1 - Create an empty word window and add a new blank document
; *******************************************************
;
#include <Word.au3>

Local $oWordApp = _WordCreate("")
Local $oDoc = _WordDocAdd($oWordApp)
