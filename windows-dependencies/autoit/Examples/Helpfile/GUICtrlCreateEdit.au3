#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <EditConstants.au3>
#include <StaticConstants.au3>

Global $oMyError

Example()
RichEditExample()

Func Example()
	Local $myedit, $msg

	GUICreate("My GUI edit") ; will create a dialog box that when displayed is centered

	$myedit = GUICtrlCreateEdit("First line" & @CRLF, 176, 32, 121, 97, $ES_AUTOVSCROLL + $WS_VSCROLL)

	GUISetState()

	Send("{END}")

	; will be append dont' forget 3rd parameter
	GUICtrlSetData($myedit, "Second line", 1)

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()

		If $msg = $GUI_EVENT_CLOSE Then ExitLoop
	WEnd
	GUIDelete()
EndFunc   ;==>Example


; Rich edit control EXAMPLE using GUICtrlCreateObj

; Author: Kåre Johansson
; AutoIt Version: 3.1.1.55
; Description: Very Simple example: Embedding RICHTEXT object
; Needs: MSCOMCT2.OCX in system32 but it's probably already there
; Date: 3 jul 2005
Func RichEditExample()
	Local $oRP, $TagsPageC, $AboutC, $PrefsC, $StatC, $GUIActiveX, $msg

	$oMyError = ObjEvent("AutoIt.Error", "MyErrFunc")

	$oRP = ObjCreate("RICHTEXT.RichtextCtrl.1")

	GUICreate("Embedded RICHTEXT control Test", 320, 200, -1, -1, BitOR($WS_OVERLAPPEDWINDOW, $WS_CLIPSIBLINGS, $WS_CLIPCHILDREN))
	$TagsPageC = GUICtrlCreateLabel('Visit Tags Page', 5, 180, 100, 15, $SS_CENTER)
	GUICtrlSetFont($TagsPageC, 9, 400, 4)
	GUICtrlSetColor($TagsPageC, 0x0000ff)
	GUICtrlSetCursor($TagsPageC, 0)
	$AboutC = GUICtrlCreateButton('About', 105, 177, 70, 20)
	$PrefsC = GUICtrlCreateButton('FontSize', 175, 177, 70, 20)
	$StatC = GUICtrlCreateButton('Plain Style', 245, 177, 70, 20)

	$GUIActiveX = GUICtrlCreateObj($oRP, 10, 10, 400, 260)
	GUICtrlSetPos($GUIActiveX, 10, 10, 300, 160)

	With $oRP; Object tag pool
		.OLEDrag()
		.Font = 'Arial'
		.text = "Hello - Au3 supports ActiveX components like the RICHTEXT thanks to SvenP" & @CRLF & 'Try write some text and quit to reload'
		;.FileName = @ScriptDir & '\RichText.rtf'
		;.BackColor = 0xff00
	EndWith

	GUISetState();Show GUI

	While 1
		$msg = GUIGetMsg()

		Select
			Case $msg = $GUI_EVENT_CLOSE
				$oRP.SaveFile(@ScriptDir & "\RichText.rtf", 0)
				ExitLoop
			Case $msg = $TagsPageC
				Run(@ComSpec & ' /c start http://www.myplugins.info/guids/typeinfo/typeinfo.php?clsid={3B7C8860-D78F-101B-B9B5-04021C009402}', '', @SW_HIDE)
			Case $msg = $AboutC
				$oRP.AboutBox()
			Case $msg = $PrefsC
				$oRP.SelFontSize = 12
			Case $msg = $StatC
				$oRP.SelBold = False
				$oRP.SelItalic = False
				$oRP.SelUnderline = False
				$oRP.SelFontSize = 8
		EndSelect
	WEnd
	GUIDelete()
EndFunc   ;==>RichEditExample

Func MyErrFunc()

	MsgBox(0, "AutoItCOM Test", "We intercepted a COM Error !" & @CRLF & @CRLF & _
			"err.description is: " & @TAB & $oMyError.description & @CRLF & _
			"err.windescription:" & @TAB & $oMyError.windescription & @CRLF & _
			"err.number is: " & @TAB & Hex($oMyError.number, 8) & @CRLF & _
			"err.lastdllerror is: " & @TAB & $oMyError.lastdllerror & @CRLF & _
			"err.scriptline is: " & @TAB & $oMyError.scriptline & @CRLF & _
			"err.source is: " & @TAB & $oMyError.source & @CRLF & _
			"err.helpfile is: " & @TAB & $oMyError.helpfile & @CRLF & _
			"err.helpcontext is: " & @TAB & $oMyError.helpcontext _
			, 5)
	; Will automatically continue after 5 seconds

	Local $err = $oMyError.number
	If $err = 0 Then $err = -1

	SetError($err) ; to check for after this function returns
EndFunc   ;==>MyErrFunc
