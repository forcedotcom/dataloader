#include <GUIConstantsEx.au3>
#include <GuiTreeView.au3>
#include <GuiImageList.au3>
#include <WindowsConstants.au3>

$Debug_TV = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

Global $hTreeView

_Main()

Func _Main()

	Local $hGui, $hItem[6], $hImage
	Local $iStyle = BitOR($TVS_EDITLABELS, $TVS_HASBUTTONS, $TVS_HASLINES, $TVS_LINESATROOT, $TVS_DISABLEDRAGDROP, $TVS_SHOWSELALWAYS, $TVS_CHECKBOXES)

	$hGui = GUICreate("TreeView Get Edit Control", 400, 300)

	$hTreeView = _GUICtrlTreeView_Create($hGui, 2, 2, 396, 268, $iStyle, $WS_EX_CLIENTEDGE)
	GUISetState()

	GUIRegisterMsg($WM_NOTIFY, "WM_NOTIFY")

	$hImage = _GUIImageList_Create(16, 16, 5, 3)
	_GUIImageList_AddIcon($hImage, "shell32.dll", 110)
	_GUIImageList_AddIcon($hImage, "shell32.dll", 131)
	_GUIImageList_AddIcon($hImage, "shell32.dll", 165)
	_GUIImageList_AddIcon($hImage, "shell32.dll", 168)
	_GUIImageList_AddIcon($hImage, "shell32.dll", 137)
	_GUIImageList_AddIcon($hImage, "shell32.dll", 146)
	_GUICtrlTreeView_SetNormalImageList($hTreeView, $hImage)

	For $x = 0 To _GUIImageList_GetImageCount($hImage) - 1
		$hItem[$x] = _GUICtrlTreeView_Add($hTreeView, 0, StringFormat("[%02d] New Item", $x + 1), $x, $x)
	Next

	; Edit item 0 label
	_GUICtrlTreeView_EditText($hTreeView, $hItem[0])
	Sleep(1000)
	_GUICtrlTreeView_EndEdit($hTreeView)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

Func WM_NOTIFY($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg, $iwParam
	Local $hWndFrom, $iIDFrom, $iCode, $tNMHDR, $hWndTreeview
	$hWndTreeview = $hTreeView
	If Not IsHWnd($hTreeView) Then $hWndTreeview = GUICtrlGetHandle($hTreeView)

	$tNMHDR = DllStructCreate($tagNMHDR, $ilParam)
	$hWndFrom = HWnd(DllStructGetData($tNMHDR, "hWndFrom"))
	$iIDFrom = DllStructGetData($tNMHDR, "IDFrom")
	$iCode = DllStructGetData($tNMHDR, "Code")
	Switch $hWndFrom
		Case $hWndTreeview
			Switch $iCode
				Case $NM_CLICK ; The user has clicked the left mouse button within the control
					_DebugPrint("$NM_CLICK" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
;~ 					Return 1 ; nonzero to not allow the default processing
					Return 0 ; zero to allow the default processing
				Case $NM_DBLCLK ; The user has double-clicked the left mouse button within the control
					_DebugPrint("$NM_DBLCLK" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
;~ 					Return 1 ; nonzero to not allow the default processing
					Return 0 ; zero to allow the default processing
				Case $NM_RCLICK ; The user has clicked the right mouse button within the control
					_DebugPrint("$NM_RCLICK" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
;~ 					Return 1 ; nonzero to not allow the default processing
					Return 0 ; zero to allow the default processing
				Case $NM_RDBLCLK ; The user has clicked the right mouse button within the control
					_DebugPrint("$NM_RDBLCLK" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
;~ 					Return 1 ; nonzero to not allow the default processing
					Return 0 ; zero to allow the default processing
				Case $NM_KILLFOCUS ; control has lost the input focus
					_DebugPrint("$NM_KILLFOCUS" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; No return value
				Case $NM_RETURN ; control has the input focus and that the user has pressed the key
					_DebugPrint("$NM_RETURN" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
;~ 					Return 1 ; nonzero to not allow the default processing
					Return 0 ; zero to allow the default processing
;~ 				Case $NM_SETCURSOR ; control is setting the cursor in response to a WM_SETCURSOR message
;~ 					Local $tinfo = DllStructCreate($tagNMMOUSE, $ilParam)
;~ 					$hWndFrom = HWnd(DllStructGetData($tinfo, "hWndFrom"))
;~ 					$iIDFrom = DllStructGetData($tinfo, "IDFrom")
;~ 					$iCode = DllStructGetData($tinfo, "Code")
;~ 					_DebugPrint("$NM_SETCURSOR" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
;~ 							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
;~ 							"-->Code:" & @TAB & $iCode & @LF & _
;~ 							"-->ItemSpec:" & @TAB & DllStructGetData($tinfo, "ItemSpec") & @LF & _
;~ 							"-->ItemData:" & @TAB & DllStructGetData($tinfo, "ItemData") & @LF & _
;~ 							"-->X:" & @TAB & DllStructGetData($tinfo, "X") & @LF & _
;~ 							"-->Y:" & @TAB & DllStructGetData($tinfo, "Y") & @LF & _
;~ 							"-->HitInfo:" & @TAB & DllStructGetData($tinfo, "HitInfo"))
;~ 					Return 0 ; to enable the control to set the cursor
;~ 					Return 1 ; nonzero to prevent the control from setting the cursor
				Case $NM_SETFOCUS ; control has received the input focus
					_DebugPrint("$NM_SETFOCUS" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; No return value
				Case $TVN_BEGINDRAGA, $TVN_BEGINDRAGW
					_DebugPrint("$TVN_BEGINDRAG")
				Case $TVN_BEGINLABELEDITA, $TVN_BEGINLABELEDITW
					_DebugPrint("$TVN_BEGINLABELEDIT")
					MsgBox(4160, "Information", "Edit Control Handle: 0x" & Hex(_GUICtrlTreeView_GetEditControl($hTreeView)) & @CRLF & _
							"IsPtr = " & IsPtr(_GUICtrlTreeView_GetEditControl($hTreeView)) & " IsHWnd = " & IsHWnd(_GUICtrlTreeView_GetEditControl($hTreeView)))
				Case $TVN_BEGINRDRAGA, $TVN_BEGINRDRAGW
					_DebugPrint("$TVN_BEGINRDRAG")
				Case $TVN_DELETEITEMA, $TVN_DELETEITEMW
					_DebugPrint("$TVN_DELETEITEM")
				Case $TVN_ENDLABELEDITA, $TVN_ENDLABELEDITW
					_DebugPrint("$TVN_ENDLABELEDIT")
					Local $tInfo = DllStructCreate($tagNMHDR & ";" & $tagTVITEMEX, $ilParam)
					If DllStructGetData($tInfo, "Text") <> 0 Then
						Local $tBuffer = DllStructCreate("char Text[" & DllStructGetData($tInfo, "TextMax") & "]", DllStructGetData($tInfo, "Text"))
						_GUICtrlTreeView_SetText($hTreeView, _GUICtrlTreeView_GetSelection($hTreeView), DllStructGetData($tBuffer, "Text"))
					EndIf
				Case $TVN_GETDISPINFOA, $TVN_GETDISPINFOW
					_DebugPrint("$TVN_GETDISPINFO")
				Case $TVN_GETINFOTIPA, $TVN_GETINFOTIPW
					_DebugPrint("$TVN_GETINFOTIP")
				Case $TVN_ITEMEXPANDEDA, $TVN_ITEMEXPANDEDW
					_DebugPrint("$TVN_ITEMEXPANDED")
				Case $TVN_ITEMEXPANDINGA, $TVN_ITEMEXPANDINGW
					_DebugPrint("$TVN_ITEMEXPANDING")
				Case $TVN_KEYDOWN
					_DebugPrint("$TVN_KEYDOWN")
				Case $TVN_SELCHANGEDA, $TVN_SELCHANGEDW
					_DebugPrint("$TVN_SELCHANGED")
				Case $TVN_SELCHANGINGA, $TVN_SELCHANGINGW
					_DebugPrint("$TVN_SELCHANGING")
				Case $TVN_SETDISPINFOA, $TVN_SETDISPINFOW
					_DebugPrint("$TVN_SETDISPINFO")
				Case $TVN_SINGLEEXPAND
					_DebugPrint("$TVN_SINGLEEXPAND")
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
