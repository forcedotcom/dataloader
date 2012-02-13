; *******************************************************
; Example 1 - Open a browser with the "form" example, get a reference
;				to the submit button by name and "click" it. This technique
;				of submitting forms is useful because many forms rely on JavaScript
;				code and "onClick" events on their submit button making _IEFormSubmit()
;				not perform as expected
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("form")
Local $oSubmit = _IEGetObjByName($oIE, "submitExample")
_IEAction($oSubmit, "click")
_IELoadWait($oIE)

; *******************************************************
; Example 2 - Same as Example 1, except instead of using click, give the element focus
;				and then use ControlSend to send Enter.  Use this technique when the
;				browser-side scripting associated with a click action prevents control
;				from being automatically returned to your code.
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example("form")
$oSubmit = _IEGetObjByName($oIE, "submitExample")
Local $hwnd = _IEPropertyGet($oIE, "hwnd")
_IEAction($oSubmit, "focus")
ControlSend($hwnd, "", "[CLASS:Internet Explorer_Server; INSTANCE:1]", "{Enter}")

; Wait for Alert window, then click on OK
WinWait("Windows Internet Explorer", "ExampleFormSubmitted")
ControlClick("Windows Internet Explorer", "ExampleFormSubmitted", "[CLASS:Button; TEXT:OK; Instance:1;]")
_IELoadWait($oIE)
