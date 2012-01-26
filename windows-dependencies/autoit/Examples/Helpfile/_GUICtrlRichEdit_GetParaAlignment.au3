#include <GuiRichEdit.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

Global $lblMsg, $hRichEdit

Main()

Func Main()
	Local $hGui, $iMsg, $iCp, $btnNext, $iStep = 0
	$hGui = GUICreate("Example (" & StringTrimRight(@ScriptName, 4) & ")", 320, 350, -1, -1)
	$hRichEdit = _GUICtrlRichEdit_Create($hGui, "This is a test.", 10, 10, 300, 220, _
			BitOR($ES_MULTILINE, $WS_VSCROLL, $ES_AUTOVSCROLL))
	$lblMsg = GUICtrlCreateLabel("", 10, 235, 300, 60)
	$btnNext = GUICtrlCreateButton("Next", 270, 310, 40, 30)
	GUISetState()

	_GUICtrlRichEdit_SetText($hRichEdit, "First paragraph")
	Report("First paragraph")
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
						_GUICtrlRichEdit_SetParaAlignment($hRichEdit, "r")
						Report("First paragraph")
					Case 2
						_GUICtrlRichEdit_AppendText($hRichEdit, @CR & "Second paragraph")
						_GUICtrlRichEdit_SetParaAlignment($hRichEdit, "c")
						Report("Second paragraph")
					Case 3
						_GUICtrlRichEdit_AppendText($hRichEdit, @CR & "Third paragraph")
						_GUICtrlRichEdit_SetParaAlignment($hRichEdit, "l")
						Report("Third paragraph")
					Case 4
						$iCp = _GUICtrlRichEdit_GetFirstCharPosOnLine($hRichEdit, 2)
						_GUICtrlRichEdit_SetSel($hRichEdit, $iCp, -1)
						Report("Alignment of paragraphs in selection")
						GUICtrlSetState($btnNext, $GUI_DISABLE)
				EndSwitch
		EndSelect
	WEnd
EndFunc   ;==>Main

Func Report($sMsg)
	$sMsg = $sMsg & @CR & @CR & "Get function returns " & @CR & _GUICtrlRichEdit_GetParaAlignment($hRichEdit)
	GUICtrlSetData($lblMsg, $sMsg)
	ControlFocus($hRichEdit, "", "")
EndFunc   ;==>Report
