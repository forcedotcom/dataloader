; *******************************************************
; Example 1 - Create a browser window and navigate to a website,
;				wait 5 seconds and navigate to another
;				wait 5 seconds and navigate to another
; *******************************************************

#include <IE.au3>

Local $oIE = _IECreate("www.autoitscript.com")
Sleep(5000)
_IENavigate($oIE, "http://www.autoitscript.com/forum/index.php?")
Sleep(5000)
_IENavigate($oIE, "http://www.autoitscript.com/forum/index.php?showforum=9")

; *******************************************************
; Example 2 - Create a browser window and navigate to a website,
;				do not wait for page load to complete before moving to next line
; *******************************************************

#include <IE.au3>

$oIE = _IECreate("www.autoitscript.com", 0)
MsgBox(0, "_IENavigate()", "This code executes immediately")
