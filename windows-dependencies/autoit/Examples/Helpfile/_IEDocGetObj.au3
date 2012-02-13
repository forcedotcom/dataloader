; *******************************************************
; Example 1 - Open a browser to the AutoIt Homepage, get a reference
;				to the document object and display a document attribute
; *******************************************************

#include <IE.au3>

Local $oIE = _IECreate("http://www.autoitscript.com")
Local $oDoc = _IEDocGetObj($oIE)
MsgBox(0, "Document Created Date", $oDoc.fileCreatedDate)
