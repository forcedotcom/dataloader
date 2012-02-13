; *******************************************************
; Example 1 - Open browser with basic example, click on the 3rd
;				link on the page (note: the first link is index 0)
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("basic")
_IELinkClickByIndex($oIE, 2)
