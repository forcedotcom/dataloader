; *******************************************************
; Example 1 - Open a browser with the form example, fill in a form field and
;				reset the form back to default values
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("form")
Local $oForm = _IEFormGetObjByName($oIE, "ExampleForm")
Local $oText = _IEFormElementGetObjByName($oForm, "textExample")
_IEFormElementSetValue($oText, "Hey! It works!")
_IEFormReset($oForm)
