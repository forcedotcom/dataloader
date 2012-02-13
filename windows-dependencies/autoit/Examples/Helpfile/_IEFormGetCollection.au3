; *******************************************************
; Example 1 - Get a reference to a specific form by 0-based index,
;				in this case the first form on the page
; *******************************************************

#include <IE.au3>

Local $oIE = _IECreate("http://www.google.com")
Local $oForm = _IEFormGetCollection($oIE, 0)
Local $oQuery = _IEFormElementGetCollection($oForm, 1)
_IEFormElementSetValue($oQuery, "AutoIt IE.au3")
_IEFormSubmit($oForm)

; *******************************************************
; Example 2 - Get a reference to the collection of forms on a page,
;				and then loop through them displaying information for each
; *******************************************************

#include <IE.au3>

$oIE = _IECreate("http://www.autoitscript.com")
Local $oForms = _IEFormGetCollection($oIE)
MsgBox(0, "Forms Info", "There are " & @extended & " forms on this page")
For $oForm In $oForms
	MsgBox(0, "Form Info", $oForm.name)
Next

; *******************************************************
; Example 3 - Get a reference to the collection of forms on a page,
;				and then loop through them displaying information for each
;				demonstrating use of form index
; *******************************************************

#include <IE.au3>

$oIE = _IECreate("http://www.autoitscript.com")
$oForms = _IEFormGetCollection($oIE)
Local $iNumForms = @extended
MsgBox(0, "Forms Info", "There are " & $iNumForms & " forms on this page")
For $i = 0 To $iNumForms - 1
	$oForm = _IEFormGetCollection($oIE, $i)
	MsgBox(0, "Form Info", $oForm.name)
Next
