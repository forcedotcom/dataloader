; *******************************************************
; Example 1 - Open a browser with the basic example, check to see if the
;				addressbar is visible, if it is not turn it on. Then change
;				the text displayed in the statusbar
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("basic")
If Not _IEPropertyGet($oIE, "statusbar") Then _IEPropertySet($oIE, "statusbar", True)
_IEPropertySet($oIE, "statustext", "Look What I can Do")
Sleep(1000)
_IEPropertySet($oIE, "statustext", "I can change the status text")
