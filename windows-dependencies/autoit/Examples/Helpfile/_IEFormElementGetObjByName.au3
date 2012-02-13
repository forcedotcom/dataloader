; *******************************************************
; Example 1 - Get a reference to a specific form element by name.
;				In this case, submit a query to the Google search engine
; *******************************************************

#include <IE.au3>

Local $oIE = _IECreate("http://www.google.com")
Local $oForm = _IEFormGetObjByName($oIE, "f")
Local $oQuery = _IEFormElementGetObjByName($oForm, "q")
_IEFormElementSetValue($oQuery, "AutoIt IE.au3")
_IEFormSubmit($oForm)
