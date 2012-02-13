; *******************************************************
; Example 1 - Open a browser with the form example, click on the
;				<input type=image> form element with matching alt text
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("form")
_IEFormImageClick($oIE, "AutoIt Homepage", "alt")

; *******************************************************
; Example 2 - Open a browser with the form example, click on the <input type=image>
;				form element with matching img source URL (sub-string)
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example("form")
_IEFormImageClick($oIE, "autoit_6_240x100.jpg", "src")

; *******************************************************
; Example 3 - Open a browser with the form example, click on the
;				<input type=image> form element with matching name
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example("form")
_IEFormImageClick($oIE, "imageExample", "name")
