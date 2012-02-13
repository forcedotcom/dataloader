#include <GuiRichEdit.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

Main()

Func Main()
	Local $hGui, $iMsg, $btnNext, $iStep = 0, $lblMsg, $hRichEdit
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
						_GUICtrlRichEdit_SetSel($hRichEdit, 5, 2)
					Case 2
						_GUICtrlRichEdit_InsertText($hRichEdit, "INSERTED_1")
						GUICtrlSetData($lblMsg, "Text is inserted at active point of selection")
					Case 3
						_GUICtrlRichEdit_GotoCharPos($hRichEdit, 20)
						_GUICtrlRichEdit_InsertText($hRichEdit, "INSERTED_2")
						GUICtrlSetData($lblMsg, "Text is inserted at insertion point")
						GUICtrlSetState($btnNext, $GUI_DISABLE)
				EndSwitch
		EndSelect
	WEnd
EndFunc   ;==>Main
