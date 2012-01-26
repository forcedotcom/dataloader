; *******************************************************
; Example 1 - Open the AutoIt forum page, tab to the "View new posts"
;				link and activate the link with the enter key.
;				Then wait for the page load to complete	before moving on.
; *******************************************************

#include <IE.au3>

Local $oIE = _IECreate("http://www.autoitscript.com/forum/index.php")
Send("{TAB 12}")
Send("{ENTER}")
_IELoadWait($oIE)
