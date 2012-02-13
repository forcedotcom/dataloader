; *******************************************************
; Example 1 - Retrieve and display Word.au3 version information
; *******************************************************
;
#include <Word.au3>

Local $aVersion = _Word_VersionInfo()
MsgBox(0, "Word.au3 Version", $aVersion[5] & " released " & $aVersion[4])
