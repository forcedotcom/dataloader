; *******************************************************
; Example 1 - Open a browser with the basic example page, insert an
;				event script into the head of the document that creates
;				a JavaScript alert when someone clicks on the document
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("basic")
_IEHeadInsertEventScript($oIE, "document", "onclick", "alert('Someone clicked the document!');")

; *******************************************************
; Example 2 - Open a browser with the basic example page, insert an
;				event script into the head of the document that creates
;				a JavaScript alert when someone tries to right-click on the
;				document and then the event script returns "false" to prevent
;				the right-click context menu from appearing
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example("basic")
_IEHeadInsertEventScript($oIE, "document", "oncontextmenu", "alert('No Context Menu');return false")

; *******************************************************
; Example 3 - Open a browser with the basic example page, insert an
;				event script into the head of the document that creates a
;				JavaScript alert when we are about to navigate away from the
;				page and presents the option to cancel the operation.
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example("basic")
_IEHeadInsertEventScript($oIE, "window", "onbeforeunload", _
		"alert('Example warning follows...');return 'Pending changes may be lost';")
_IENavigate($oIE, "www.autoitscript.com")

; *******************************************************
; Example 4 - Open a browser with the basic example page, insert an
;				event script into the head of the document that prevents
;				selection of text in the document
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example()
_IEHeadInsertEventScript($oIE, "document", "ondrag", "return false;")
_IEHeadInsertEventScript($oIE, "document", "onselectstart", "return false;")

; *******************************************************
; Example 5 - Open a browser with the AutoIt homepage, insert an
;				event script into the head of the document that prevents
;				navigation when any link is clicked and log the URL of the
;               clicked link to the console
; *******************************************************

#include <IE.au3>

$oIE = _IECreate("http://www.autoitscript.com")
Local $oLinks = _IELinkGetCollection($oIE)
For $oLink In $oLinks
	Local $sLinkId = _IEPropertyGet($oLink, "uniqueid")
	_IEHeadInsertEventScript($oIE, $sLinkId, "onclick", "return false;")
	ObjEvent($oLink, "_Evt_")
Next

While 1
	Sleep(100)
WEnd

Func _Evt_onClick()
	Local $o_link = @COM_EventObj
	ConsoleWrite($o_link.href & @CRLF)
EndFunc   ;==>_Evt_onClick
