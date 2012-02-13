; *******************************************************
; Example 1 - Open a browser to the form example, get an object reference
;				to the element with the name "ExampleForm".  In this case the
;				result is identical to using $oForm = _IEFormGetObjByName($oIE, "ExampleForm")
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("form")
Local $oForm = _IEGetObjByName($oIE, "ExampleForm")
