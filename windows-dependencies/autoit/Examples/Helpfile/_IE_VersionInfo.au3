; *******************************************************
; Example 1 - Retrieve and display IE.au3 version information
; *******************************************************

#include <IE.au3>

Local $aVersion = _IE_VersionInfo()
MsgBox(0, "IE.au3 Version", $aVersion[5] & " released " & $aVersion[4])
