; *******************************************************
; Example 1 - Get a reference to a specific form element by 0-based index.
;				In this case, submit a query to the Google search engine
; *******************************************************

#include <IE.au3>

Local $oIE = _IECreate("http://www.google.com")
Local $oForm = _IEFormGetCollection($oIE, 0)
Local $oQuery = _IEFormElementGetCollection($oForm, 2)
_IEFormElementSetValue($oQuery, "AutoIt IE.au3")
_IEFormSubmit($oForm)
