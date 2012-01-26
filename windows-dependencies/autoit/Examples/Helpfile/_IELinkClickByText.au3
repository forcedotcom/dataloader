; *******************************************************
; Example 1 - Open browser with basic example, click on the	link
;				with text "user forum"
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("basic")
_IELinkClickByText($oIE, "user forum")

; *******************************************************
; Example 2 - Open browser to the AutoIt homepage, loop through the links
;				on the page and click on the link with text "wallpaper"
;				using a sub-string match.
; *******************************************************

#include <IE.au3>

$oIE = _IECreate("http://www.autoitscript.com")

Local $sMyString = "wallpaper"
Local $oLinks = _IELinkGetCollection($oIE)
For $oLink In $oLinks
	Local $sLinkText = _IEPropertyGet($oLink, "innerText")
	If StringInStr($sLinkText, $sMyString) Then
		_IEAction($oLink, "click")
		ExitLoop
	EndIf
Next
