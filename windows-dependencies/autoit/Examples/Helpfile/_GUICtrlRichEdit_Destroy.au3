#include <GuiRichEdit.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

Global $hRichEdit

Main()

Func Main()
	Local $hGui, $iMsg, $btnDoIt
	$hGui = GUICreate("Example (" & StringTrimRight(@ScriptName, 4) & ")", 320, 350, -1, -1)
	$hRichEdit = _GUICtrlRichEdit_Create($hGui, "This is a test.", 10, 10, 300, 220, _
			BitOR($ES_MULTILINE, $WS_VSCROLL, $ES_AUTOVSCROLL))
	$btnDoIt = GUICtrlCreateButton("Do it", 10, 260, 90, 25)

	GUISetState()

	While True
		$iMsg = GUIGetMsg()
		Select
			Case $iMsg = $GUI_EVENT_CLOSE
				GUIDelete() ; needed unless script crashes if DoIt as not been pushed
				Exit
			Case $iMsg = $btnDoIt
				_GUICtrlRichEdit_Destroy($hRichEdit)
		EndSelect
	WEnd
EndFunc   ;==>Main
