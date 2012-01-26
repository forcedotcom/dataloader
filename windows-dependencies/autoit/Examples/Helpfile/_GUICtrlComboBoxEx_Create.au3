#include <GuiComboBoxEx.au3>
#include <WindowsConstants.au3>
#include <GUIConstantsEx.au3>

$Debug_CB = False ; Check ClassName being passed to ComboBox/ComboBoxEx functions, set to True and use a handle to another control to see it work

Global $hCombo

_Main()

Func _Main()
	Local $hGUI

	; Create GUI
	$hGUI = GUICreate("ComboBoxEx Create", 400, 300)
	$hCombo = _GUICtrlComboBoxEx_Create($hGUI, "This is a test|Line 2", 2, 2, 394, 268)
	GUISetState()

	GUIRegisterMsg($WM_NOTIFY, "WM_NOTIFY")

	_GUICtrlComboBoxEx_AddString($hCombo, "Some More Text")
	_GUICtrlComboBoxEx_InsertString($hCombo, "Inserted Text", 1)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

Func WM_NOTIFY($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg, $iwParam
	Local $hWndFrom, $iIDFrom, $iCode, $tNMHDR, $tInfo

	$tNMHDR = DllStructCreate($tagNMHDR, $ilParam)
	$hWndFrom = HWnd(DllStructGetData($tNMHDR, "hWndFrom"))
	$iIDFrom = DllStructGetData($tNMHDR, "IDFrom")
	$iCode = DllStructGetData($tNMHDR, "Code")
	Switch $hWndFrom
		Case $hCombo
			Switch $iCode
				Case $CBEN_BEGINEDIT ; Sent when the user activates the drop-down list or clicks in the control's edit box.
					_DebugPrint("$CBEN_BEGINEDIT" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					Return 0
				Case $CBEN_DELETEITEM
					_DebugPrint("$CBEN_DELETEITEM" & _GetComboBoxEx($ilParam))
					Return 0
				Case $CBEN_DRAGBEGINA, $CBEN_DRAGBEGINW
					$tInfo = DllStructCreate($tagNMCBEDRAGBEGIN, $ilParam)
					If DllStructGetData($tInfo, "ItemID") Then _DebugPrint("$CBEN_DRAGBEGIN" & _GetComboBoxEx($ilParam))
					_DebugPrint("$CBEN_DRAGBEGIN" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tInfo, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tInfo, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tInfo, "Code") & @LF & _
							"-->ItemID:" & @TAB & DllStructGetData($tInfo, "ItemID") & @LF & _
							"-->Text:" & @TAB & DllStructGetData($tInfo, "Text"))
					; return is ignored
				Case $CBEN_ENDEDITA, $CBEN_ENDEDITW ; Sent when the user has concluded an operation within the edit box or has selected an item from the control's drop-down list.
					$tInfo = DllStructCreate($tagNMCBEENDEDIT, $ilParam)
					_DebugPrint("$CBEN_ENDEDIT" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tInfo, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tInfo, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tInfo, "Code") & @LF & _
							"-->fChanged:" & @TAB & DllStructGetData($tInfo, "fChanged") & @LF & _
							"-->NewSelection:" & @TAB & DllStructGetData($tInfo, "NewSelection") & @LF & _
							"-->Text:" & @TAB & DllStructGetData($tInfo, "Text") & @LF & _
							"-->Why:" & @TAB & DllStructGetData($tInfo, "Why"))
					Return False ; accept the notification and allow the control to display the selected item
;~ 					Return True  ; otherwise
				Case $CBEN_GETDISPINFOA, $CBEN_GETDISPINFOW ; Sent to retrieve display information about a callback item
					_DebugPrint("$CBEN_GETDISPINFO" & _GetComboBoxEx($ilParam))
					Return 0
				Case $CBEN_INSERTITEM
					$tInfo = DllStructCreate($tagNMCOMBOBOXEX, $ilParam)
					Local $tBuffer = DllStructCreate("wchar Text[" & DllStructGetData($tInfo, "TextMax") & "]", DllStructGetData($tInfo, "Text"))
					_DebugPrint("$CBEN_INSERTITEM" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tInfo, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tInfo, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tInfo, "Code") & @LF & _
							"-->Mask:" & @TAB & DllStructGetData($tInfo, "Mask") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tInfo, "Item") & @LF & _
							"-->Text:" & @TAB & DllStructGetData($tBuffer, "Text") & @LF & _
							"-->TextMax:" & @TAB & DllStructGetData($tInfo, "TextMax") & @LF & _
							"-->Indent:" & @TAB & DllStructGetData($tInfo, "Indent") & @LF & _
							"-->Image:" & @TAB & DllStructGetData($tInfo, "Image") & @LF & _
							"-->SelectedImage:" & @TAB & DllStructGetData($tInfo, "SelectedImage") & @LF & _
							"-->OverlayImage:" & @TAB & DllStructGetData($tInfo, "OverlayImage") & @LF & _
							"-->Param:" & @TAB & DllStructGetData($tInfo, "Param"))
					Return 0
			EndSwitch
	EndSwitch
	Return $GUI_RUNDEFMSG
EndFunc   ;==>WM_NOTIFY

Func _GetComboBoxEx($ilParam)
	Local $tInfo = DllStructCreate($tagNMCOMBOBOXEX, $ilParam)
	Local $aItem = _GUICtrlComboBoxEx_GetItem($hCombo, DllStructGetData($tInfo, "Item"))
	Return @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tInfo, "hWndFrom") & @LF & _
			"-->IDFrom:" & @TAB & DllStructGetData($tInfo, "IDFrom") & @LF & _
			"-->Code:" & @TAB & DllStructGetData($tInfo, "Code") & @LF & _
			"-->Mask:" & @TAB & DllStructGetData($tInfo, "Mask") & @LF & _
			"-->Item:" & @TAB & DllStructGetData($tInfo, "Item") & @LF & _
			"-->Text:" & @TAB & $aItem[0] & @LF & _
			"-->TextMax:" & @TAB & $aItem[1] & @LF & _
			"-->Indent:" & @TAB & $aItem[2] & @LF & _
			"-->Image:" & @TAB & $aItem[3] & @LF & _
			"-->SelectedImage:" & @TAB & $aItem[4] & @LF & _
			"-->OverlayImage:" & @TAB & $aItem[5] & @LF & _
			"-->Param:" & @TAB & $aItem[5]
EndFunc   ;==>_GetComboBoxEx

Func _DebugPrint($s_text, $line = @ScriptLineNumber)
	ConsoleWrite( _
			"!===========================================================" & @LF & _
			"+======================================================" & @LF & _
			"-->Line(" & StringFormat("%04d", $line) & "):" & @TAB & $s_text & @LF & _
			"+======================================================" & @LF)
EndFunc   ;==>_DebugPrint
