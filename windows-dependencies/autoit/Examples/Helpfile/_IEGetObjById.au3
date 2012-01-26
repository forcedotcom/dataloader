; *******************************************************
; Example 1 - Open a browser to the basic example, get an object reference
;				to the DIV element with the ID "line1". Display the innerText
;				of this element to the console.
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("basic")
Local $oDiv = _IEGetObjById($oIE, "line1")
ConsoleWrite(_IEPropertyGet($oDiv, "innertext") & @CRLF)
