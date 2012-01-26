; *******************************************************
; Example 1 - Get a reference to a specific form by name.  In this case, submit a query
;				to the Google search engine.  Note that the names of the form and form
;				elements can be found by viewing the page HTML source
; *******************************************************

#include <IE.au3>

Local $oIE = _IECreate("http://www.google.com")
Local $oForm = _IEFormGetObjByName($oIE, "f")
Local $oQuery = _IEFormElementGetObjByName($oForm, "q")
_IEFormElementSetValue($oQuery, "AutoIt IE.au3")
_IEFormSubmit($oForm)
