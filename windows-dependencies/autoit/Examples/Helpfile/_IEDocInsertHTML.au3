; *******************************************************
; Example 1 - Insert HTML at the top and bottom of a document
; *******************************************************

#include <IE.au3>

Local $oIE = _IECreate("http://www.autoitscript.com")
Local $oBody = _IETagNameGetCollection($oIE, "body", 0)
_IEDocInsertHTML($oBody, "<h2>This HTML is inserted After Begin</h2>", "afterbegin")
_IEDocInsertHTML($oBody, "<h2>This HTML is inserted Before End</h2>", "beforeend")

; *******************************************************
; Example 2 - Open a browser with the basic example page, insert HTML
;		in and around the DIV tag named "IEAu3Data" and display Body HTML
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example("basic")
Local $oDiv = _IEGetObjByName($oIE, "IEAu3Data")

_IEDocInsertHTML($oDiv, "<b>(HTML beforebegin)</b>", "beforebegin")
_IEDocInsertHTML($oDiv, "<i>(HTML afterbegin)</i>", "afterbegin")
_IEDocInsertHTML($oDiv, "<b>(HTML beforeend)</b>", "beforeend")
_IEDocInsertHTML($oDiv, "<i>(HTML afterend)</i>", "afterend")

ConsoleWrite(_IEBodyReadHTML($oIE) & @CRLF)

; *******************************************************
; Example 3 - Advanced example
;		Insert a clock and a referrer string at the top of every page, even when you
;		browse to a new location.  Uses _IEDocInsertText, _IEDocInsertHTML and
;		_IEPropertySet features "innerhtml" and "referrer"
; *******************************************************

#include <IE.au3>

$oIE = _IECreate("http://www.autoitscript.com")

AdlibRegister("UpdateClock", 1000) ; Update clock once per second

; idle as long as the browser window exists
While WinExists(_IEPropertyGet($oIE, "hwnd"))
	Sleep(10000)
WEnd

Exit

Func UpdateClock()
	Local $curTime = "<b>Current Time is: </b>" & @HOUR & ":" & @MIN & ":" & @SEC
	; _IEGetObjByName is expected to return a NoMatch error after navigation
	;   (before DIV is inserted), so temporarily turn off notification
	_IEErrorNotify(False)
	Local $oAutoItClock = _IEGetObjByName($oIE, "AutoItClock")
	If Not IsObj($oAutoItClock) Then ; Insert DIV element if it wasn't found
		;
		; Get reference to BODY, insert DIV, get reference to DIV, update time
		Local $oBody = _IETagNameGetCollection($oIE, "body", 0)
		_IEDocInsertHTML($oBody, "<div id='AutoItClock'></div>", "afterbegin")
		$oAutoItClock = _IEGetObjByName($oIE, "AutoItClock")
		_IEPropertySet($oAutoItClock, "innerhtml", $curTime)
		;
		; Check referrer string, if not blank insert after clock
		_IELoadWait($oIE)
		Local $sReferrer = _IEPropertyGet($oIE, "referrer")
		If $sReferrer Then _IEDocInsertText($oAutoItClock, _
				"  Referred by: " & $sReferrer, "afterend")
	Else
		_IEPropertySet($oAutoItClock, "innerhtml", $curTime) ; update time
	EndIf
	_IEErrorNotify(True)
EndFunc   ;==>UpdateClock
