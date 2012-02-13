; *******************************************************
; Example 1 - Open browser with basic example, get link collection,
;				loop through items and display the associated link URL references
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("basic")
Local $oLinks = _IELinkGetCollection($oIE)
Local $iNumLinks = @extended
MsgBox(0, "Link Info", $iNumLinks & " links found")
For $oLink In $oLinks
	MsgBox(0, "Link Info", $oLink.href)
Next
