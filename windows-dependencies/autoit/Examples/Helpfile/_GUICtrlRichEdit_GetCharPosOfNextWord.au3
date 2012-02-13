#include <GuiRichEdit.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

Global $lblMsg, $hRichEdit

Main()

Func Main()
	Local $hGui, $iMsg, $btnNext, $iCp = 0
	$hGui = GUICreate("Example (" & StringTrimRight(@ScriptName, 4) & ")", 320, 350, -1, -1)
	$hRichEdit = _GUICtrlRichEdit_Create($hGui, "This is a test.", 10, 10, 300, 220, _
			BitOR($ES_MULTILINE, $WS_VSCROLL, $ES_AUTOVSCROLL))
	$lblMsg = GUICtrlCreateLabel("", 10, 235, 300, 60)
	$btnNext = GUICtrlCreateButton("Next", 270, 310, 40, 30)
	GUISetState()

	_GUICtrlRichEdit_AppendText($hRichEdit, "AutoIt v3 is a freeware BASIC-like scripting language designed for " _
			 & "automating the Windows GUI and general scripting.")

	While True
		$iMsg = GUIGetMsg()
		Select
			Case $iMsg = $GUI_EVENT_CLOSE
				_GUICtrlRichEdit_Destroy($hRichEdit) ; needed unless script crashes
;~ 				GUIDelete() 	; is OK too
				Exit
			Case $iMsg = $btnNext
				$iCp = _GUICtrlRichEdit_GetCharPosOfNextWord($hRichEdit, $iCp)
				GUICtrlSetData($lblMsg, $iCp)
				ControlFocus($hRichEdit, "", "")
				_GUICtrlRichEdit_GotoCharPos($hRichEdit, $iCp)
		EndSelect
	WEnd
EndFunc   ;==>Main
