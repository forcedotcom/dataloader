; *******************************************************
; Example 1 - Open a browser with the basic example, ready the body HTML,
;				append new HTML to the original and write it back to the browser
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("basic")
Local $sHTML = _IEBodyReadHTML($oIE)
$sHTML = $sHTML & "<p><font color=red size=+5>Big RED text!</font>"
_IEBodyWriteHTML($oIE, $sHTML)
