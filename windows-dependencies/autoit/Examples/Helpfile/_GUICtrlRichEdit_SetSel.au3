#include <GuiRichEdit.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

Global $lblMsg, $hRichEdit

Main()

Func Main()
	Local $hGui, $iMsg, $btnNext, $iStep = 0, $iCp1
	$hGui = GUICreate("Example (" & StringTrimRight(@ScriptName, 4) & ")", 320, 350, -1, -1)
	$hRichEdit = _GUICtrlRichEdit_Create($hGui, "This is a test.", 10, 10, 300, 220, _
			BitOR($ES_MULTILINE, $WS_VSCROLL, $ES_AUTOVSCROLL, $ES_NOHIDESEL))
	$lblMsg = GUICtrlCreateLabel("", 10, 235, 300, 60)
	$btnNext = GUICtrlCreateButton("Next", 270, 310, 40, 30)
	GUISetState()

	While True
		$iMsg = GUIGetMsg()
		Select
			Case $iMsg = $GUI_EVENT_CLOSE
				_GUICtrlRichEdit_Destroy($hRichEdit) ; needed unless script crashes
;~ 				GUIDelete() 	; is OK too
				Exit
			Case $iMsg = $btnNext
				$iStep += 1
				Switch $iStep
					Case 1
						_GUICtrlRichEdit_SetSel($hRichEdit, 0, 3)
						Report("1. Initial Character attributes at start of line 1 are")
					Case 2
						_GUICtrlRichEdit_AutoDetectURL($hRichEdit, True)
						_GUICtrlRichEdit_AppendText($hRichEdit, @CR & "http://www.autoitscript.com")
						$iCp1 = _GUICtrlRichEdit_GetFirstCharPosOnLine($hRichEdit, 2)
						_GUICtrlRichEdit_SetSel($hRichEdit, $iCp1, $iCp1 + 3)
						Report("2. Character attributes at start of line 2 are")
						GUICtrlSetState($btnNext, $GUI_DISABLE)
				EndSwitch
		EndSelect
	WEnd
EndFunc   ;==>Main

Func Report($sMsg)
	Local $sRet = _GUICtrlRichEdit_GetCharAttributes($hRichEdit)
	$sMsg = $sMsg & @CR & @CR & "Char Attributes=" & $sRet
	GUICtrlSetData($lblMsg, $sMsg)
EndFunc   ;==>Report
