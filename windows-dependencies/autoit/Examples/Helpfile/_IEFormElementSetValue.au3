; *******************************************************
; Example 1 - Open a browser with the form example, set the value of a text form element
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("form")
Local $oForm = _IEFormGetObjByName($oIE, "ExampleForm")
Local $oText = _IEFormElementGetObjByName($oForm, "textExample")
_IEFormElementSetValue($oText, "Hey! This works!")

; *******************************************************
; Example 2 - Get a reference to a specific form element and set its value.
;				In this case, submit a query to the Google search engine
; *******************************************************

#include <IE.au3>

$oIE = _IECreate("http://www.google.com")
$oForm = _IEFormGetObjByName($oIE, "f")
Local $oQuery = _IEFormElementGetObjByName($oForm, "q")
_IEFormElementSetValue($oQuery, "AutoIt IE.au3")
_IEFormSubmit($oForm)

; *******************************************************
; Example 3 - Set the value of an INPUT TYPE=TEXT element using Send()
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example("form")
$oForm = _IEFormGetObjByName($oIE, "ExampleForm")
Local $oInputFile = _IEFormElementGetObjByName($oForm, "textExample")

; Assign input focus to the field and then send the text string
_IEAction($oInputFile, "focus")

; Select existing content so it will be overwritten.
_IEAction($oInputFile, "selectall")

Send("This works")

; *******************************************************
; Example 4 - Set the value of an INPUT TYPE=TEXT element on an invisible
;				window using ControlSend()
; *******************************************************
;
#include <IE.au3>

$oIE = _IE_Example("form")

; Hide the browser window to demonstrate sending text to invisible window
_IEAction($oIE, "invisible")

$oForm = _IEFormGetObjByName($oIE, "ExampleForm")
$oInputFile = _IEFormElementGetObjByName($oForm, "textExample")

; Assign input focus to the field and then send the text string
_IEAction($oInputFile, "focus")

; Select existing content so it will be overwritten.
_IEAction($oInputFile, "selectall")

; Get a handle to the IE window.
Local $hIE = _IEPropertyGet($oIE, "hwnd")
ControlSend($hIE, "", "[CLASS:Internet Explorer_Server; INSTANCE:1]", "This works")

MsgBox(0, "Success", "Value set to 'This works'")
_IEAction($oIE, "visible")
