; *******************************************************
; Example 1 - Open a browser with the basic example, check to see if the
;				addressbar is visible, if it is turn it off, if it is not turn it on
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("basic")
If _IEPropertyGet($oIE, "addressbar") Then
	MsgBox(0, "AddressBar Status", "AddressBar Visible, turning it off")
	_IEPropertySet($oIE, "addressbar", False)
Else
	MsgBox(0, "AddressBar Status", "AddressBar Invisible, turning it on")
	_IEPropertySet($oIE, "addressbar", True)
EndIf

; *******************************************************
; Example 2 - Open a browser with the form example and get a reference to the form
;				textarea element.  Get the coordinates and dimensions of the text area,
;				outline its shape with the mouse and come to rest in the center
; *******************************************************

$oIE = _IE_Example("form")

Local $oForm = _IEFormGetObjByName($oIE, "ExampleForm")
Local $oTextArea = _IEFormElementGetObjByName($oForm, "textareaExample")

; Get coordinates and dimensions of the textarea
Local $iScreenX = _IEPropertyGet($oTextArea, "screenx")
Local $iScreenY = _IEPropertyGet($oTextArea, "screeny")
Local $iBrowserX = _IEPropertyGet($oTextArea, "browserx")
Local $iBrowserY = _IEPropertyGet($oTextArea, "browserY")
Local $iWidth = _IEPropertyGet($oTextArea, "width")
Local $iHeight = _IEPropertyGet($oTextArea, "height")

; Outline the textarea with the mouse, come to rest in the center
MouseMove($iScreenX, $iScreenY)
MouseMove($iScreenX + $iWidth, $iScreenY)
MouseMove($iScreenX + $iWidth, $iScreenY + $iHeight)
MouseMove($iScreenX, $iScreenY + $iHeight)
MouseMove($iScreenX, $iScreenY)
MouseMove($iScreenX + $iWidth / 2, $iScreenY + $iHeight / 2)
