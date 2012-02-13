; *******************************************************
; Example 1 - Open a browser with the form example, get reference to form, select and
;				deselect the checkboxes byValue.  Since $s_Name is not specified, operate
;				on the collection of all <input type=checkbox> elements in the form
;				Note: You will likely need to scroll down on the page to see the changes
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("form")
Local $oForm = _IEFormGetObjByName($oIE, "ExampleForm")
For $i = 1 To 5
	_IEFormElementCheckBoxSelect($oForm, "gameBasketball", "", 1, "byValue")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, "gameFootball", "", 1, "byValue")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, "gameTennis", "", 1, "byValue")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, "gameBaseball", "", 1, "byValue")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, "gameBasketball", "", 0, "byValue")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, "gameFootball", "", 0, "byValue")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, "gameTennis", "", 0, "byValue")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, "gameBaseball", "", 0, "byValue")
	Sleep(1000)
Next

; *******************************************************
; Example 2 - Open a browser with the form example, get reference to form, select and
;				deselect the checkboxes byIndex.  Since $s_Name is not specified, operate
;				on the collection of all <input type=checkbox> elements in the form
;				Note: You will likely need to scroll down on the page to see the changes
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example("form")
$oForm = _IEFormGetObjByName($oIE, "ExampleForm")
For $i = 1 To 5
	_IEFormElementCheckBoxSelect($oForm, 3, "", 1, "byIndex")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, 2, "", 1, "byIndex")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, 1, "", 1, "byIndex")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, 0, "", 1, "byIndex")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, 3, "", 0, "byIndex")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, 2, "", 0, "byIndex")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, 1, "", 0, "byIndex")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, 0, "", 0, "byIndex")
	Sleep(1000)
Next

; *******************************************************
; Example 3 - Open a browser with the form example, get reference to form, select and
;				deselect the checkboxes byIndex in the group that shares the name checkboxG2Example
;				Note: You will likely need to scroll down on the page to see the changes
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example("form")
$oForm = _IEFormGetObjByName($oIE, "ExampleForm")
For $i = 1 To 5
	_IEFormElementCheckBoxSelect($oForm, 0, "checkboxG2Example", 1, "byIndex")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, 1, "checkboxG2Example", 1, "byIndex")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, 0, "checkboxG2Example", 0, "byIndex")
	Sleep(1000)
	_IEFormElementCheckBoxSelect($oForm, 1, "checkboxG2Example", 0, "byIndex")
	Sleep(1000)
Next
