; *******************************************************
; Example 1 - Create a browser window and navigate to a website
; *******************************************************

#include <IE.au3>

Local $oIE = _IECreate("www.autoitscript.com")

; *******************************************************
; Example 2 - Create new browser windows pointing to each of 3 different URLs
;				if one does not already exist ($f_tryAttach = 1)
;				do not wait for the page loads to complete ($f_wait = 0)
; *******************************************************

#include <IE.au3>

_IECreate("www.autoitscript.com", 1, 1, 0)
_IECreate("my.yahoo.com", 1, 1, 0)
_IECreate("www.google.com", 1, 1, 0)

; *******************************************************
; Example 3 - Attempt to attach to an existing browser displaying a particular website URL
;				Create a new browser and navigate to that site if one does not already exist
; *******************************************************

#include <IE.au3>

$oIE = _IECreate("www.autoitscript.com", 1)
; Check @extended return value to see if attach was successful
If @extended Then
	MsgBox(0, "", "Attached to Existing Browser")
Else
	MsgBox(0, "", "Created New Browser")
EndIf

; *******************************************************
; Example 4 - Create an empty browser window and populate it with custom HTML
; *******************************************************

#include <IE.au3>

$oIE = _IECreate()
Local $sHTML = "<h1>Hello World!</h1>"
_IEBodyWriteHTML($oIE, $sHTML)

; *******************************************************
; Example 5 - Create an invisible browser window, navigate to a website,
;				retrieve some information and Quit
; *******************************************************

#include <IE.au3>

$oIE = _IECreate("http://sourceforge.net", 0, 0)
; Display the innerText on an element on the page with a name of "sfmarquee"
Local $oMarquee = _IEGetObjByName($oIE, "sfmarquee")
MsgBox(0, "SourceForge Information", $oMarquee.innerText)
_IEQuit($oIE)

; *******************************************************
; Example 6 - Create a browser window attached to a new instance of iexplore.exe
;				This is often necessary in order to get a new session cookie context
;				(session cookies are shared among all browser instances sharing the same iexplore.exe)
; *******************************************************

#include <IE.au3>

ShellExecute("iexplore.exe", "about:blank")
WinWait("Blank Page")
$oIE = _IEAttach("about:blank", "url")
_IELoadWait($oIE)
_IENavigate($oIE, "www.autoitscript.com")
