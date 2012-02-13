; *******************************************************
; Example 1 - Open a browser with the form example, get reference to form, get reference
;				to select element, cycle 10 times selecting options byValue, byText and byIndex
;				Note: You will likely need to scroll down on the page to see the changes
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("form")
Local $oForm = _IEFormGetObjByName($oIE, "ExampleForm")
Local $oSelect = _IEFormElementGetObjByName($oForm, "selectExample")
For $i = 1 To 10
	_IEFormElementOptionSelect($oSelect, "Freepage", 1, "byText")
	Sleep(1000)
	_IEFormElementOptionSelect($oSelect, "midipage.html", 1, "byValue")
	Sleep(1000)
	_IEFormElementOptionSelect($oSelect, 0, 1, "byIndex")
	Sleep(1000)
Next

; *******************************************************
; Example 2 - Open a browser with the form example, get reference to form, get reference
;				to select multiple element, cycle 5 times selecting and then deselecting
;				options byValue, byText and byIndex.
;				Note: You will likely need to scroll down on the page to see the changes
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example("form")
$oForm = _IEFormGetObjByName($oIE, "ExampleForm")
$oSelect = _IEFormElementGetObjByName($oForm, "multipleSelectExample")
For $i = 1 To 5
	_IEFormElementOptionSelect($oSelect, "Carlos", 1, "byText")
	Sleep(1000)
	_IEFormElementOptionSelect($oSelect, "Name2", 1, "byValue")
	Sleep(1000)
	_IEFormElementOptionSelect($oSelect, 5, 1, "byIndex")
	Sleep(1000)
	_IEFormElementOptionSelect($oSelect, "Carlos", 0, "byText")
	Sleep(1000)
	_IEFormElementOptionSelect($oSelect, "Name2", 0, "byValue")
	Sleep(1000)
	_IEFormElementOptionSelect($oSelect, 5, 0, "byIndex")
	Sleep(1000)
Next

; *******************************************************
; Example 3 - Open a browser with the form example, get reference to form, get reference
;				to select element, check to see if the option "Freepage" is selected and
;				report result.  Repeat for the option with index 0 and for the option
;				with value of 'midipage.html'
;				Note: You will likely need to scroll down on the page to see the changes
; *******************************************************
;
#include <IE.au3>
$oIE = _IE_Example("form")
$oForm = _IEFormGetObjByName($oIE, "ExampleForm")
$oSelect = _IEFormElementGetObjByName($oForm, "selectExample")
If _IEFormElementOptionSelect($oSelect, "Freepage", -1, "byText") Then
	MsgBox(0, "Option Selected", "Option Freepage is selected")
Else
	MsgBox(0, "Option Selected", "Option Freepage is Not selected")
EndIf
If _IEFormElementOptionSelect($oSelect, 0, -1, "byIndex") Then
	MsgBox(0, "Option Selected", "The First (index 0) option is selected")
Else
	MsgBox(0, "Option Selected", "The First (index 0) option is Not selected")
EndIf
If _IEFormElementOptionSelect($oSelect, "midipage.html", -1, "byValue") Then
	MsgBox(0, "Option Selected", "The option with value 'midipage.html' is selected")
Else
	MsgBox(0, "Option Selected", "The option with value 'midipage.html' is NOT selected")
EndIf
