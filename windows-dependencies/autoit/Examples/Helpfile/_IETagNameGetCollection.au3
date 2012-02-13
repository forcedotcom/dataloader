; *******************************************************
; Example 1 - Open a browser with the form example, get the collection
;				of all INPUT tags and display the formname and type of each
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("form")
Local $oInputs = _IETagNameGetCollection($oIE, "input")
For $oInput In $oInputs
	MsgBox(0, "Form Input Type", "Form: " & $oInput.form.name & " Type: " & $oInput.type)
Next
