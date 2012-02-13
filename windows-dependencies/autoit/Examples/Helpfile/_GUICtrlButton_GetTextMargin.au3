#include <GUIConstantsEx.au3>
#include <GuiButton.au3>
#include <WindowsConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $y = 70, $btn[6], $aMargins

	GUICreate("Buttons", 510, 400)
	$iMemo = GUICtrlCreateEdit("", 119, 10, 276, 374, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	$btn[0] = GUICtrlCreateButton("Button1", 10, 10, 90, 50)
	_GUICtrlButton_SetTextMargin($btn[0], 10, 5, 10, 5)

	For $x = 1 To 5
		$btn[$x] = GUICtrlCreateButton("Button" & $x + 1, 10, $y, 90, 50)
		$y += 60
	Next

	For $x = 0 To 5
		$aMargins = _GUICtrlButton_GetTextMargin($btn[$x])
		MemoWrite("Button" & $x + 1 & " Margins:" & @CRLF & @TAB & _
				"Left.: " & $aMargins[0] & @TAB & "Top...: " & $aMargins[1] & @CRLF & @TAB & _
				"Right: " & $aMargins[2] & @TAB & "Bottom: " & $aMargins[3] & @CRLF)
	Next

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
		EndSwitch
	WEnd

	Exit
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
