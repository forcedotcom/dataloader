#include <GUIConstantsEx.au3>
#include <GuiStatusBar.au3>
#include <ComboConstants.au3>
#include <EditConstants.au3>
#include <WindowsConstants.au3>

$Debug_SB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

Global $iMemo, $MainGUI, $hStatus

Example1()
Example2()
Example3()
Example4()
Example5()
Example6()

Func Example1()

	Local $hGUI
	Local $aParts[3] = [75, 150, -1]

	; Create GUI
	$hGUI = GUICreate("(Example 1) StatusBar Create", 400, 300)

	;===============================================================================
	; defaults to 1 part, no text
	$hStatus = _GUICtrlStatusBar_Create($hGUI)
	;===============================================================================
	_GUICtrlStatusBar_SetParts($hStatus, $aParts)

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 274, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUICtrlSendMsg($iMemo, $EM_SETREADONLY, True, 0)
	GUICtrlSetBkColor($iMemo, 0xFFFFFF)
	MemoWrite("StatusBar Created with:" & @CRLF & _
			@TAB & "Handle to GUI window" & @CRLF)
	GUISetState()

	GUIRegisterMsg($WM_NOTIFY, "WM_NOTIFY")

	; Get border sizes
	MemoWrite("Horizontal border width .: " & _GUICtrlStatusBar_GetBordersHorz($hStatus))
	MemoWrite("Vertical border width ...: " & _GUICtrlStatusBar_GetBordersVert($hStatus))
	MemoWrite("Width between rectangles : " & _GUICtrlStatusBar_GetBordersRect($hStatus))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUISetState(@SW_ENABLE, $MainGUI)
	GUIDelete($hGUI)
EndFunc   ;==>Example1

Func Example2()

	Local $hGUI
	Local $aParts[3] = [75, 150, -1]

	; Create GUI
	$hGUI = GUICreate("(Example 2) StatusBar Create", 400, 300)

	;===============================================================================
	; sets parts with no text
	$hStatus = _GUICtrlStatusBar_Create($hGUI, $aParts)
	;===============================================================================

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 274, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUICtrlSendMsg($iMemo, $EM_SETREADONLY, True, 0)
	GUICtrlSetBkColor($iMemo, 0xFFFFFF)
	MemoWrite("StatusBar Created with:" & @CRLF & _
			@TAB & "Handle to GUI window" & @CRLF & _
			@TAB & "part width array of 3 elements" & @CRLF)
	GUISetState()

	GUIRegisterMsg($WM_NOTIFY, "WM_NOTIFY")

	; Get border sizes
	MemoWrite("Horizontal border width .: " & _GUICtrlStatusBar_GetBordersHorz($hStatus))
	MemoWrite("Vertical border width ...: " & _GUICtrlStatusBar_GetBordersVert($hStatus))
	MemoWrite("Width between rectangles : " & _GUICtrlStatusBar_GetBordersRect($hStatus))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUISetState(@SW_ENABLE, $MainGUI)
	GUIDelete($hGUI)
EndFunc   ;==>Example2

Func Example3()

	Local $hGUI
	Local $aText[3] = ["Left Justified", @TAB & "Centered", @TAB & @TAB & "Right Justified"]
	Local $aParts[3] = [100, 175, -1]

	; Create GUI
	$hGUI = GUICreate("(Example 3) StatusBar Create", 400, 300)

	;===============================================================================
	;sets parts and text
	$hStatus = _GUICtrlStatusBar_Create($hGUI, $aParts, $aText)
	;===============================================================================

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 274, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUICtrlSendMsg($iMemo, $EM_SETREADONLY, True, 0)
	GUICtrlSetBkColor($iMemo, 0xFFFFFF)
	MemoWrite("StatusBar Created with:" & @CRLF & _
			@TAB & "only Handle," & @CRLF & _
			@TAB & "part width array of 3 elements" & @CRLF & _
			@TAB & "part text array of 3 elements" & @CRLF)
	GUISetState()

	GUIRegisterMsg($WM_NOTIFY, "WM_NOTIFY")

	; Get border sizes
	MemoWrite("Horizontal border width .: " & _GUICtrlStatusBar_GetBordersHorz($hStatus))
	MemoWrite("Vertical border width ...: " & _GUICtrlStatusBar_GetBordersVert($hStatus))
	MemoWrite("Width between rectangles : " & _GUICtrlStatusBar_GetBordersRect($hStatus))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUISetState(@SW_ENABLE, $MainGUI)
	GUIDelete($hGUI)
EndFunc   ;==>Example3

Func Example4()

	Local $hGUI
	Local $aText[3] = ["Left Justified", @TAB & "Centered", @TAB & @TAB & "Right Justified"]

	; Create GUI
	$hGUI = GUICreate("(Example 4) StatusBar Create", 400, 300)

	;===============================================================================
	; will create part widths based on part size passed in
	$hStatus = _GUICtrlStatusBar_Create($hGUI, 100, $aText)
	;===============================================================================

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 274, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUICtrlSendMsg($iMemo, $EM_SETREADONLY, True, 0)
	GUICtrlSetBkColor($iMemo, 0xFFFFFF)
	MemoWrite("StatusBar Created with:" & @CRLF & _
			@TAB & "only Handle," & @CRLF & _
			@TAB & "part width number" & @CRLF & _
			@TAB & "part text array of 3 elements" & @CRLF)
	GUISetState()

	GUIRegisterMsg($WM_NOTIFY, "WM_NOTIFY")

	; Get border sizes
	MemoWrite("Horizontal border width .: " & _GUICtrlStatusBar_GetBordersHorz($hStatus))
	MemoWrite("Vertical border width ...: " & _GUICtrlStatusBar_GetBordersVert($hStatus))
	MemoWrite("Width between rectangles : " & _GUICtrlStatusBar_GetBordersRect($hStatus))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUISetState(@SW_ENABLE, $MainGUI)
	GUIDelete($hGUI)
EndFunc   ;==>Example4

Func Example5()

	Local $hGUI
	Local $aText[3] = ["Left Justified", @TAB & "Centered", @TAB & @TAB & "Right Justified"]
	Local $aParts[2] = [100, 175]

	; Create GUI
	$hGUI = GUICreate("(Example 5) StatusBar Create", 400, 300)


	;===============================================================================
	; adjusts array to largest array passed in (this time the text array is the largest)
	$hStatus = _GUICtrlStatusBar_Create($hGUI, $aParts, $aText)
	;===============================================================================

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 274, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUICtrlSendMsg($iMemo, $EM_SETREADONLY, True, 0)
	GUICtrlSetBkColor($iMemo, 0xFFFFFF)
	MemoWrite("StatusBar Created with:" & @CRLF & _
			@TAB & "only Handle," & @CRLF & _
			@TAB & "part width array of 2 elements" & @CRLF & _
			@TAB & "part text array of 3 elements" & @CRLF)
	GUISetState()

	GUIRegisterMsg($WM_NOTIFY, "WM_NOTIFY")

	; Get border sizes
	MemoWrite("Horizontal border width .: " & _GUICtrlStatusBar_GetBordersHorz($hStatus))
	MemoWrite("Vertical border width ...: " & _GUICtrlStatusBar_GetBordersVert($hStatus))
	MemoWrite("Width between rectangles : " & _GUICtrlStatusBar_GetBordersRect($hStatus))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUISetState(@SW_ENABLE, $MainGUI)
	GUIDelete($hGUI)
EndFunc   ;==>Example5

Func Example6()

	Local $hGUI
	Local $aText[2] = ["Left Justified", @TAB & "Centered"]
	Local $aParts[3] = [100, 175, -1]

	; Create GUI
	$hGUI = GUICreate("(Example 6) StatusBar Create", 400, 300)

	;===============================================================================
	; adjusts array to largest array passed in (this time the part width array)
	$hStatus = _GUICtrlStatusBar_Create($hGUI, $aParts, $aText)
	;===============================================================================

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 274, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUICtrlSendMsg($iMemo, $EM_SETREADONLY, True, 0)
	GUICtrlSetBkColor($iMemo, 0xFFFFFF)
	MemoWrite("StatusBar Created with:" & @CRLF & _
			@TAB & "only Handle," & @CRLF & _
			@TAB & "part width array of 3 elements" & @CRLF & _
			@TAB & "part text array of 2 elements" & @CRLF)
	GUISetState()

	GUIRegisterMsg($WM_NOTIFY, "WM_NOTIFY")

	; Get border sizes
	MemoWrite("Horizontal border width .: " & _GUICtrlStatusBar_GetBordersHorz($hStatus))
	MemoWrite("Vertical border width ...: " & _GUICtrlStatusBar_GetBordersVert($hStatus))
	MemoWrite("Width between rectangles : " & _GUICtrlStatusBar_GetBordersRect($hStatus))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUISetState(@SW_ENABLE, $MainGUI)
	GUIDelete($hGUI)
EndFunc   ;==>Example6

; Write message to memo
Func MemoWrite($sMessage = "")
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite

Func WM_NOTIFY($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg, $iwParam
	Local $hWndFrom, $iIDFrom, $iCode, $tNMHDR

	$tNMHDR = DllStructCreate($tagNMHDR, $ilParam)
	$hWndFrom = HWnd(DllStructGetData($tNMHDR, "hWndFrom"))
	$iIDFrom = DllStructGetData($tNMHDR, "IDFrom")
	$iCode = DllStructGetData($tNMHDR, "Code")
	Local $tinfo
	Switch $hWndFrom
		Case $hStatus
			Switch $iCode
				Case $NM_CLICK ; The user has clicked the left mouse button within the control
					$tinfo = DllStructCreate($tagNMMOUSE, $ilParam)
					$hWndFrom = HWnd(DllStructGetData($tinfo, "hWndFrom"))
					$iIDFrom = DllStructGetData($tinfo, "IDFrom")
					$iCode = DllStructGetData($tinfo, "Code")
					_DebugPrint("$NM_CLICK" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode & @LF & _
							"-->ItemSpec:" & @TAB & DllStructGetData($tinfo, "ItemSpec") & @LF & _
							"-->ItemData:" & @TAB & DllStructGetData($tinfo, "ItemData") & @LF & _
							"-->X:" & @TAB & DllStructGetData($tinfo, "X") & @LF & _
							"-->Y:" & @TAB & DllStructGetData($tinfo, "Y") & @LF & _
							"-->HitInfo:" & @TAB & DllStructGetData($tinfo, "HitInfo"))
					Return True ; indicate that the mouse click was handled and suppress default processing by the system
;~ 					Return FALSE ;to allow default processing of the click.
				Case $NM_DBLCLK ; The user has double-clicked the left mouse button within the control
					$tinfo = DllStructCreate($tagNMMOUSE, $ilParam)
					$hWndFrom = HWnd(DllStructGetData($tinfo, "hWndFrom"))
					$iIDFrom = DllStructGetData($tinfo, "IDFrom")
					$iCode = DllStructGetData($tinfo, "Code")
					_DebugPrint("$NM_DBLCLK" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode & @LF & _
							"-->ItemSpec:" & @TAB & DllStructGetData($tinfo, "ItemSpec") & @LF & _
							"-->ItemData:" & @TAB & DllStructGetData($tinfo, "ItemData") & @LF & _
							"-->X:" & @TAB & DllStructGetData($tinfo, "X") & @LF & _
							"-->Y:" & @TAB & DllStructGetData($tinfo, "Y") & @LF & _
							"-->HitInfo:" & @TAB & DllStructGetData($tinfo, "HitInfo"))
					Return True ; indicate that the mouse click was handled and suppress default processing by the system
;~ 					Return FALSE ;to allow default processing of the click.
				Case $NM_RCLICK ; The user has clicked the right mouse button within the control
					$tinfo = DllStructCreate($tagNMMOUSE, $ilParam)
					$hWndFrom = HWnd(DllStructGetData($tinfo, "hWndFrom"))
					$iIDFrom = DllStructGetData($tinfo, "IDFrom")
					$iCode = DllStructGetData($tinfo, "Code")
					_DebugPrint("$NM_RCLICK" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode & @LF & _
							"-->ItemSpec:" & @TAB & DllStructGetData($tinfo, "ItemSpec") & @LF & _
							"-->ItemData:" & @TAB & DllStructGetData($tinfo, "ItemData") & @LF & _
							"-->X:" & @TAB & DllStructGetData($tinfo, "X") & @LF & _
							"-->Y:" & @TAB & DllStructGetData($tinfo, "Y") & @LF & _
							"-->HitInfo:" & @TAB & DllStructGetData($tinfo, "HitInfo"))
					Return True ; indicate that the mouse click was handled and suppress default processing by the system
;~ 					Return FALSE ;to allow default processing of the click.
				Case $NM_RDBLCLK ; The user has clicked the right mouse button within the control
					$tinfo = DllStructCreate($tagNMMOUSE, $ilParam)
					$hWndFrom = HWnd(DllStructGetData($tinfo, "hWndFrom"))
					$iIDFrom = DllStructGetData($tinfo, "IDFrom")
					$iCode = DllStructGetData($tinfo, "Code")
					_DebugPrint("$NM_RDBLCLK" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode & @LF & _
							"-->ItemSpec:" & @TAB & DllStructGetData($tinfo, "ItemSpec") & @LF & _
							"-->ItemData:" & @TAB & DllStructGetData($tinfo, "ItemData") & @LF & _
							"-->X:" & @TAB & DllStructGetData($tinfo, "X") & @LF & _
							"-->Y:" & @TAB & DllStructGetData($tinfo, "Y") & @LF & _
							"-->HitInfo:" & @TAB & DllStructGetData($tinfo, "HitInfo"))
					Return True ; indicate that the mouse click was handled and suppress default processing by the system
;~ 					Return FALSE ;to allow default processing of the click.
				Case $SBN_SIMPLEMODECHANGE ; Simple mode changes
					_DebugPrint("$SBN_SIMPLEMODECHANGE" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; No return value
			EndSwitch
	EndSwitch
	Return $GUI_RUNDEFMSG
EndFunc   ;==>WM_NOTIFY

Func _DebugPrint($s_text, $line = @ScriptLineNumber)
	ConsoleWrite( _
			"!===========================================================" & @LF & _
			"+======================================================" & @LF & _
			"-->Line(" & StringFormat("%04d", $line) & "):" & @TAB & $s_text & @LF & _
			"+======================================================" & @LF)
EndFunc   ;==>_DebugPrint
