; *******************************************************
; Example 1 - Attach to a browser with "AutoIt" in its title, display the URL
; *******************************************************

#include <IE.au3>

Local $oIE = _IEAttach("AutoIt")
MsgBox(0, "The URL", _IEPropertyGet($oIE, "locationurl"))

; *******************************************************
; Example 2 - Attach to a browser with "The quick brown fox"
;				in the text of it's top-level document
; *******************************************************

#include <IE.au3>

$oIE = _IEAttach("The quick brown fox", "text")

; *******************************************************
; Example 3 - Attach to a browser control embedded in another window
; *******************************************************

#include <IE.au3>

$oIE = _IEAttach("A Window Title", "embedded")

; *******************************************************
; Example 4 - Attach to the 3rd browser control embedded in another window
;				Use the advanced window title syntax to use the 2nd window
;				with the string 'ICQ' in the title
; *******************************************************

#include <IE.au3>

$oIE = _IEAttach("[REGEXPTITLE:ICQ; INSTANCE:2]", "embedded", 3)

; *******************************************************
; Example 5 - Create an array of object references to all current browser instances
;				The first array element will contain the number of instances found
; *******************************************************

#include <IE.au3>

Local $aIE[1]
$aIE[0] = 0

Local $i = 1
While 1
	$oIE = _IEAttach("", "instance", $i)
	If @error = $_IEStatus_NoMatch Then ExitLoop
	ReDim $aIE[$i + 1]
	$aIE[$i] = $oIE
	$aIE[0] = $i
	$i += 1
WEnd

MsgBox(0, "Browsers Found", "Number of browser instances in the array: " & $aIE[0])
