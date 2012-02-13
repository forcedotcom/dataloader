#include <WindowsConstants.au3>
#include <GUIConstantsEx.au3>
#include <Timers.au3>
#include <GuiStatusBar.au3>
#include <ProgressConstants.au3>

Global $iMemo, $hStatusBar, $progress, $percent = 0, $direction = 1
Global $iTimer1, $iTimer2

_Example_Events()

Func _Example_Events()
	Local $hGUI, $btn_change, $iWait = 10, $btn_state
	Local $aParts[3] = [75, 330, -1]

	$hGUI = GUICreate("Timers Using WM_TIMER Events", 400, 320)
	$iMemo = GUICtrlCreateEdit("", 2, 32, 396, 226, BitOR($WS_HSCROLL, $WS_VSCROLL))
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	$btn_state = GUICtrlCreateButton("Start Progress Bar", 70, 270, 100, 25)
	$btn_change = GUICtrlCreateButton("Change", 215, 270, 90, 25)
	GUICtrlSetState($btn_change, $GUI_DISABLE)
	$hStatusBar = _GUICtrlStatusBar_Create($hGUI, $aParts)
	_GUICtrlStatusBar_SetText($hStatusBar, "Timers")
	_GUICtrlStatusBar_SetText($hStatusBar, @TAB & @TAB & StringFormat("%02d:%02d:%02d", @HOUR, @MIN, @SEC), 2)
	$progress = GUICtrlCreateProgress(0, 0, -1, -1, $PBS_SMOOTH)
	GUICtrlSetColor($progress, 0xff0000)
	_GUICtrlStatusBar_EmbedControl($hStatusBar, 1, GUICtrlGetHandle($progress))
	GUISetState()

	GUIRegisterMsg($WM_TIMER, "WM_TIMER")

	$iTimer1 = _Timer_SetTimer($hGUI, 1000)

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
			Case $btn_state
				If GUICtrlRead($btn_state) = "Start Progress Bar" Then
					$iTimer2 = _Timer_SetTimer($hGUI, $iWait) ; create timer
					If @error Or $iTimer2 = 0 Then ContinueLoop
					GUICtrlSetData($btn_state, "Stop Progress Bar")
					GUICtrlSetState($btn_change, $GUI_ENABLE)
				Else
					GUICtrlSetState($btn_change, $GUI_DISABLE)
					_Timer_KillTimer($hGUI, $iTimer2)
					GUICtrlSetData($btn_state, "Start Progress Bar")
				EndIf

			Case $btn_change
				If $iWait = 10 Then
					$iWait = 250
				Else
					$iWait = 10
				EndIf
				MemoWrite("Timer for _UpdateProgressBar set at: " & $iWait & " milliseconds")
				$iTimer2 = _Timer_SetTimer($hGUI, $iWait, "", $iTimer2) ; reuse timer with different interval
		EndSwitch
	WEnd

	ConsoleWrite("Killed All Timers? " & _Timer_KillAllTimers($hGUI) & @CRLF)
	GUIDelete()
EndFunc   ;==>_Example_Events

; Timer Events
Func WM_TIMER($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg, $ilParam

	Switch _Timer_GetTimerID($iwParam)
		Case $iTimer1
			_UpdateStatusBarClock()
		Case $iTimer2
			_UpdateProgressBar()
	EndSwitch
	Return $GUI_RUNDEFMSG
EndFunc   ;==>WM_TIMER

Func _UpdateStatusBarClock()
	_GUICtrlStatusBar_SetText($hStatusBar, @TAB & @TAB & StringFormat("%02d:%02d:%02d", @HOUR, @MIN, @SEC), 2)
EndFunc   ;==>_UpdateStatusBarClock

Func _UpdateProgressBar()
	$percent += 5 * $direction
	GUICtrlSetData($progress, $percent)
	If $percent = 100 Or $percent = 0 Then $direction *= -1
	If $percent = 100 Then
		GUICtrlSetColor($progress, 0xff0000)
	ElseIf $percent = 0 Then
		GUICtrlSetColor($progress, 0x0000ff)
	EndIf
EndFunc   ;==>_UpdateProgressBar

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
