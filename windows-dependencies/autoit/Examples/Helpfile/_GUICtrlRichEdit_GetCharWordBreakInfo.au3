#include <GuiRichEdit.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

Global $lblMsg, $hRichEdit

Main()

Func Main()
	Local $hGui, $iMsg, $btnNext, $iCp = -1, $s
	$hGui = GUICreate("Example (" & StringTrimRight(@ScriptName, 4) & ")", 320, 350, -1, -1)
	$hRichEdit = _GUICtrlRichEdit_Create($hGui, "This is a test.", 10, 10, 300, 220, _
			BitOR($ES_MULTILINE, $WS_VSCROLL, $ES_AUTOVSCROLL))
	$lblMsg = GUICtrlCreateLabel("", 10, 235, 300, 60)
	$btnNext = GUICtrlCreateButton("Next", 270, 310, 40, 30)
	GUISetState()

	$s = Chr(9)
	For $i = 32 To 126
		$s &= Chr($i)
	Next
	_GUICtrlRichEdit_AppendText($hRichEdit, $s & @CR)
	_GUICtrlRichEdit_AppendText($hRichEdit, "AutoIt v3 is a (freeware) BASIC-like scripting language designed for " _
			 & "automating the Windows GUI and general scripting.")
	_GUICtrlRichEdit_AppendText($hRichEdit, @CRLF & "Another paragraph")
	While True
		$iMsg = GUIGetMsg()
		Select
			Case $iMsg = $GUI_EVENT_CLOSE
				_GUICtrlRichEdit_Destroy($hRichEdit) ; needed unless script crashes
;~ 				GUIDelete() 	; is OK too
				Exit
			Case $iMsg = $btnNext
				$iCp += 1
				_GUICtrlRichEdit_GotoCharPos($hRichEdit, $iCp)
				GUICtrlSetData($lblMsg, _GUICtrlRichEdit_GetCharWordBreakInfo($hRichEdit, $iCp))
				ControlFocus($hRichEdit, "", "")
		EndSelect
	WEnd
EndFunc   ;==>Main

Func Report($sMsg)
	GUICtrlSetData($lblMsg, $sMsg)
EndFunc   ;==>Report

Func GetWord($hWnd, $iCp, $fForward, $fStartOfWord, $fClass = False)
	Local $iRet, $iWparam
	If $fClass Then
		If $fForward Then
			$iWparam = $WB_MOVEWORDRIGHT
		Else
			$iWparam = $WB_MOVEWORDLEFT
		EndIf
	Else
		If $fForward Then
			If $fStartOfWord Then
				$iWparam = $WB_RIGHT
			Else
				$iWparam = $WB_LEFT
			EndIf
		Else
			If $fStartOfWord Then
				$iWparam = $WB_RIGHTBREAK
			Else
				$iWparam = $wb_LEFTBREAK
			EndIf
		EndIf
	EndIf
	$iRet = _SendMessage($hWnd, $EM_FINDWORDBREAK, $iWparam, $iCp)
	ConsoleWrite('@@ Debug(' & @ScriptLineNumber & ') : $iWparam = ' & $iWparam & @CRLF & '>Error code: ' & @error & @CRLF) ;### Debug Console
	Return $iRet
EndFunc   ;==>GetWord
