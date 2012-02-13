#include <GuiEdit.au3>
#include <WinAPI.au3> ; used for Lo/Hi word
#include <WindowsConstants.au3>
#include <GUIConstantsEx.au3>

$Debug_Ed = False ; Check ClassName being passed to Edit functions, set to True and use a handle to another control to see it work

Global $hEdit

_Example1()
_Example2()

Func _Example1()
	Local $hGUI

	; Create GUI
	$hGUI = GUICreate("Edit Create", 400, 300)
	$hEdit = _GUICtrlEdit_Create($hGUI, "This is a test" & @CRLF & "Another Line", 2, 2, 394, 268)
	GUISetState()

	GUIRegisterMsg($WM_COMMAND, "WM_COMMAND")

	_GUICtrlEdit_AppendText($hEdit, @CRLF & "Append to the end?")

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Example1

Func _Example2()
	Local $hGUI

	; Create GUI
	$hGUI = GUICreate("Edit Create", 400, 300)
	$hEdit = _GUICtrlEdit_Create($hGUI, "", 2, 2, 394, 268)
	GUISetState()

	GUIRegisterMsg($WM_COMMAND, "WM_COMMAND")

	_GUICtrlEdit_SetText($hEdit, "This is a test" & @CRLF & "Another Line")

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Example2

Func WM_COMMAND($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg
	Local $hWndFrom, $iIDFrom, $iCode, $hWndEdit
	If Not IsHWnd($hEdit) Then $hWndEdit = GUICtrlGetHandle($hEdit)
	$hWndFrom = $ilParam
	$iIDFrom = _WinAPI_LoWord($iwParam)
	$iCode = _WinAPI_HiWord($iwParam)
	Switch $hWndFrom
		Case $hEdit, $hWndEdit
			Switch $iCode
				Case $EN_ALIGN_LTR_EC ; Sent when the user has changed the edit control direction to left-to-right
					_DebugPrint("$EN_ALIGN_LTR_EC" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $EN_ALIGN_RTL_EC ; Sent when the user has changed the edit control direction to right-to-left
					_DebugPrint("$EN_ALIGN_RTL_EC" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $EN_CHANGE ; Sent when the user has taken an action that may have altered text in an edit control
					_DebugPrint("$EN_CHANGE" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $EN_ERRSPACE ; Sent when an edit control cannot allocate enough memory to meet a specific request
					_DebugPrint("$EN_ERRSPACE" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $EN_HSCROLL ; Sent when the user clicks an edit control's horizontal scroll bar
					_DebugPrint("$EN_HSCROLL" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $EN_KILLFOCUS ; Sent when an edit control loses the keyboard focus
					_DebugPrint("$EN_KILLFOCUS" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $EN_MAXTEXT ; Sent when the current text insertion has exceeded the specified number of characters for the edit control
					_DebugPrint("$EN_MAXTEXT" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; This message is also sent when an edit control does not have the $ES_AUTOHSCROLL style and the number of characters to be
					; inserted would exceed the width of the edit control.
					; This message is also sent when an edit control does not have the $ES_AUTOVSCROLL style and the total number of lines resulting
					; from a text insertion would exceed the height of the edit control

					; no return value
				Case $EN_SETFOCUS ; Sent when an edit control receives the keyboard focus
					_DebugPrint("$EN_SETFOCUS" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $EN_UPDATE ; Sent when an edit control is about to redraw itself
					_DebugPrint("$EN_UPDATE" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $EN_VSCROLL ; Sent when the user clicks an edit control's vertical scroll bar or when the user scrolls the mouse wheel over the edit control
					_DebugPrint("$EN_VSCROLL" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
			EndSwitch
	EndSwitch
	Return $GUI_RUNDEFMSG
EndFunc   ;==>WM_COMMAND

Func _DebugPrint($s_text, $line = @ScriptLineNumber)
	ConsoleWrite( _
			"!===========================================================" & @LF & _
			"+======================================================" & @LF & _
			"-->Line(" & StringFormat("%04d", $line) & "):" & @TAB & $s_text & @LF & _
			"+======================================================" & @LF)
EndFunc   ;==>_DebugPrint
