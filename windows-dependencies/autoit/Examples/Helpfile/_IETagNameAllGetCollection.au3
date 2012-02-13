; *******************************************************
; Example 1 - Open a browser with the basic example, get the collection
;				of all elements and display the tagname and innerText of each
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("basic")
Local $oElements = _IETagNameAllGetCollection($oIE)
For $oElement In $oElements
	MsgBox(0, "Element Info", "Tagname: " & $oElement.tagname & @CR & "innerText: " & $oElement.innerText)
Next
