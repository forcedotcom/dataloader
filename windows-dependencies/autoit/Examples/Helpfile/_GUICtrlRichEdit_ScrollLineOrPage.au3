#include <GuiRichEdit.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

Global $lblMsg, $hRichEdit

Main()

Func Main()
	Local $hGui, $iMsg, $btnNext, $iStep = 0
	$hGui = GUICreate("Example (" & StringTrimRight(@ScriptName, 4) & ")", 320, 350, -1, -1)
	$hRichEdit = _GUICtrlRichEdit_Create($hGui, "This is a test.", 10, 10, 300, 220, _
			BitOR($ES_MULTILINE, $WS_VSCROLL, $ES_AUTOVSCROLL))
	$lblMsg = GUICtrlCreateLabel("", 10, 235, 300, 60)
	$btnNext = GUICtrlCreateButton("Next", 270, 310, 40, 30)
	GUISetState()

	For $i = 1 To 20
		_GUICtrlRichEdit_AppendText($hRichEdit, "Line " & $i & @CR)
	Next
	_GUICtrlRichEdit_AppendText($hRichEdit, "Line 21")

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
						DoIt("lu", "Scrolled up a line")
					Case 2
						DoIt("pu", "Scrolled up a page")
					Case 3
						DoIt("pd", "Scrolled down a page")
					Case 4
						DoIt("pd", "Tried to scroll down another page")
						GUICtrlSetState($btnNext, $GUI_DISABLE)
				EndSwitch
		EndSelect
	WEnd
EndFunc   ;==>Main

Func DoIt($sAction, $sMsg)
	Local $iQlines
	$iQlines = _GUICtrlRichEdit_ScrollLineOrPage($hRichEdit, $sAction)
	GUICtrlSetData($lblMsg, $sMsg & @CR & @CR & "Actually scrolled " & @CR & $iQlines & " lines")
EndFunc   ;==>DoIt
