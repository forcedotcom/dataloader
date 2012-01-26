#include <GUIConstantsEx.au3>
#include <GuiStatusBar.au3>
#include <WinAPI.au3>
#include <GuiImageList.au3>
#include <WindowsConstants.au3>

$Debug_SB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

Global $hStatus, $iMemo

_Main()

Func _Main()

	Local $hGUI, $hIcons[2], $hImage
	Local $aParts[4] = [75, 150, 300, 400]

	; Create GUI
	$hGUI = GUICreate("ImageList Destroy Icon", 400, 300)
	$hStatus = _GUICtrlStatusBar_Create($hGUI)

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 274, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	GUIRegisterMsg($WM_NOTIFY, "WM_NOTIFY")

	; Set parts
	_GUICtrlStatusBar_SetParts($hStatus, $aParts)
	_GUICtrlStatusBar_SetText($hStatus, "Part 1")
	_GUICtrlStatusBar_SetText($hStatus, "Part 2", 1)

	; Load images
	$hImage = _GUIImageList_Create(11, 11)
	_GUIImageList_Add($hImage, _WinAPI_CreateSolidBitmap($hStatus, 0xFF0000, 11, 11))
	_GUIImageList_Add($hImage, _WinAPI_CreateSolidBitmap($hStatus, 0x00FF00, 11, 11))
	_GUIImageList_Add($hImage, _WinAPI_CreateSolidBitmap($hStatus, 0x0000FF, 11, 11))

	; Set icons
	$hIcons[0] = _GUIImageList_GetIcon($hImage, 1)
	$hIcons[1] = _GUIImageList_GetIcon($hImage, 2)
	_GUICtrlStatusBar_SetIcon($hStatus, 0, $hIcons[0])
	_GUICtrlStatusBar_SetIcon($hStatus, 1, $hIcons[1])

	; Show icon handles
	MemoWrite("Part 1 icon handle .: 0x" & Hex($hIcons[0]))
	MemoWrite("Part 2 icon handle .: 0x" & Hex($hIcons[1]))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	; Free icons
	MsgBox(4096, "Information", "Icon 1 Destroyed? " & _GUIImageList_DestroyIcon($hIcons[0]))
	MsgBox(4096, "Information", "Icon 2 Destroyed? " & _GUIImageList_DestroyIcon($hIcons[1]))
	GUIDelete()
EndFunc   ;==>_Main

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
