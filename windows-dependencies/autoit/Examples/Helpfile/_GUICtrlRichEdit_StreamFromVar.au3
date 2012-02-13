#include <GuiRichEdit.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

Main()

Func Main()
	Local $hGui, $iMsg, $btnNext, $iStep = 0, $lblMsg, $hRichEdit, $s
	$hGui = GUICreate("Example (" & StringTrimRight(@ScriptName, 4) & ")", 320, 350, -1, -1)
	$hRichEdit = _GUICtrlRichEdit_Create($hGui, "This is a test.", 10, 10, 300, 220, _
			BitOR($ES_MULTILINE, $WS_VSCROLL, $ES_AUTOVSCROLL))
	$lblMsg = GUICtrlCreateLabel("", 10, 235, 300, 60)
	$btnNext = GUICtrlCreateButton("Next", 270, 310, 40, 30)
	GUISetState()

	_GUICtrlRichEdit_SetText($hRichEdit, "First paragraph")
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
						$s = _GUICtrlRichEdit_StreamToVar($hRichEdit)
						GUICtrlSetData($lblMsg, "Streamed to variable")
					Case 2
						_GUICtrlRichEdit_SetText($hRichEdit, "")
						_GUICtrlRichEdit_StreamFromVar($hRichEdit, $s)
						GUICtrlSetData($lblMsg, "Streamed from variable")
					Case 3
						_GUICtrlRichEdit_SetSel($hRichEdit, 2, 6)
						_GUICtrlRichEdit_StreamFromVar($hRichEdit, $s)
						GUICtrlSetData($lblMsg, "Replaced selection: an intentional mess!")
						GUICtrlSetState($btnNext, $GUI_DISABLE)
				EndSwitch
		EndSelect
	WEnd
EndFunc   ;==>Main
