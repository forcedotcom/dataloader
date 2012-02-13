; *******************************************************
; Example 1 - Open a browser with the form example, set the value of a text
;				form element, retrieve and display the value from the element
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("form")
Local $oForm = _IEFormGetObjByName($oIE, "ExampleForm")
Local $oText = _IEFormElementGetObjByName($oForm, "textExample")
Local $IEAu3Version = _IE_VersionInfo()
_IEFormElementSetValue($oText, $IEAu3Version[5])
MsgBox(0, "Form Element Value", _IEFormElementGetValue($oText))
