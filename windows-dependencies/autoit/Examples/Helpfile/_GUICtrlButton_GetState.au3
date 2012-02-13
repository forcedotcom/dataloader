#include <GUIConstantsEx.au3>
#include <GuiButton.au3>
#include <WindowsConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $rdo, $rdo2, $chk, $chk2, $chk3, $btn, $btn2

	GUICreate("Buttons", 400, 400)
	$iMemo = GUICtrlCreateEdit("", 119, 10, 276, 374, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")

	$btn = GUICtrlCreateButton("Button1", 10, 10, 90, 25)
	_GUICtrlButton_SetState($btn)

	$btn2 = GUICtrlCreateButton("Button2", 10, 60, 90, 25)

	$rdo = GUICtrlCreateRadio("Radio1", 10, 110, 90, 25)
	_GUICtrlButton_SetFocus($rdo)

	$rdo2 = GUICtrlCreateRadio("Radio2", 10, 170, 90, 25)
	_GUICtrlButton_SetCheck($rdo2)

	$chk = GUICtrlCreateCheckbox("Check1", 10, 230, 90, 25, BitOR($BS_AUTO3STATE, $BS_NOTIFY))
	_GUICtrlButton_SetCheck($chk, $BST_INDETERMINATE)

	$chk2 = GUICtrlCreateCheckbox("Check2", 10, 290, 90, 25, BitOR($BS_AUTO3STATE, $BS_NOTIFY))

	$chk3 = GUICtrlCreateCheckbox("Check3", 10, 350, 90, 25, BitOR($BS_AUTO3STATE, $BS_NOTIFY))
	_GUICtrlButton_SetCheck($chk3, $BST_CHECKED)

	GUISetState()

	MemoWrite("Button1 status:" & @CRLF & "--------------------------------" & _
			_ExplainState(_GUICtrlButton_GetState($btn), True))
	MemoWrite("Button2 status:" & @CRLF & "--------------------------------" & _
			_ExplainState(_GUICtrlButton_GetState($btn2), True))
	MemoWrite("Radio1 status: " & @CRLF & "--------------------------------" & _
			_ExplainState(_GUICtrlButton_GetState($rdo)))
	MemoWrite("Radio2 status: " & @CRLF & "--------------------------------" & _
			_ExplainState(_GUICtrlButton_GetState($rdo2)))
	MemoWrite("Check1 status: " & @CRLF & "--------------------------------" & _
			_ExplainState(_GUICtrlButton_GetState($chk)))
	MemoWrite("Check2 status: " & @CRLF & "--------------------------------" & _
			_ExplainState(_GUICtrlButton_GetState($chk2)))
	MemoWrite("Check3 status: " & @CRLF & "--------------------------------" & _
			_ExplainState(_GUICtrlButton_GetState($chk3)))

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

Func _ExplainState($iState, $fPushButton = False)
	Local $sText = ""
	If Not $fPushButton And Not $iState Then Return _
			@CRLF & "Indicates the button is cleared. Same as a return value of zero." & @CRLF
	If BitAND($iState, $BST_CHECKED) = $BST_CHECKED Then _
			$sText &= @CRLF & "Indicates the button is checked." & @CRLF
	If BitAND($iState, $BST_FOCUS) = $BST_FOCUS Then _
			$sText &= @CRLF & "Specifies the focus state. A nonzero value indicates that the button has the keyboard focus." & @CRLF
	If BitAND($iState, $BST_INDETERMINATE) = $BST_INDETERMINATE Then _
			$sText &= @CRLF & "Indicates the button is grayed because the state of the button is indeterminate." & @CRLF
	If $fPushButton Then
		If BitAND($iState, $BST_PUSHED) = $BST_PUSHED Then
			$sText &= @CRLF & "Specifies the highlight state." & @CRLF
		Else
			$sText &= @CRLF & "Specifies not highlighted state." & @CRLF
		EndIf
	EndIf
	Return $sText
EndFunc   ;==>_ExplainState
