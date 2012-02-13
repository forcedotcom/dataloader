; *******************************************************
; Example 1 - Open a browser with the basic example, read the document HTML
;				(all HTML including the <HEAD> and script) and display it in a MsgBox
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("basic")
Local $sHTML = _IEDocReadHTML($oIE)
MsgBox(0, "Document Source", $sHTML)
