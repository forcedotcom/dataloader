#include <GUIConstantsEx.au3>
#include <GuiHeader.au3>
#include <WindowsConstants.au3>

$Debug_HDR = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

Global $hHeader

_Main()

Func _Main()
	Local $hGUI

	; Create GUI
	$hGUI = GUICreate("Header", 400, 300)
	$hHeader = _GUICtrlHeader_Create($hGUI)
	GUISetState()

	GUIRegisterMsg($WM_NOTIFY, "WM_NOTIFY")

	; Add columns
	_GUICtrlHeader_AddItem($hHeader, "Column 1", 100)
	_GUICtrlHeader_AddItem($hHeader, "Column 2", 100)
	_GUICtrlHeader_AddItem($hHeader, "Column 3", 100)
	_GUICtrlHeader_AddItem($hHeader, "Column 4", 100)

	; Clear all filters
	_GUICtrlHeader_ClearFilterAll($hHeader)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

Func WM_NOTIFY($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg, $iwParam
	Local $hWndFrom, $iCode
	Local $tNMHDR, $tNMHEADER, $tNMHDFILTERBTNCLICK, $tNMHDDISPINFO

	$tNMHDR = DllStructCreate($tagNMHDR, $ilParam)
	$hWndFrom = HWnd(DllStructGetData($tNMHDR, "hWndFrom"))
	$iCode = DllStructGetData($tNMHDR, "Code")
	Switch $hWndFrom
		Case $hHeader
			Switch $iCode
				Case $HDN_BEGINDRAG ; Sent by a header control when a drag operation has begun on one of its items
					$tNMHEADER = DllStructCreate($tagNMHEADER, $ilParam)
					_DebugPrint("$HDN_BEGINDRAG" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMHEADER, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMHEADER, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMHEADER, "Code") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tNMHEADER, "Item") & @LF & _
							"-->Button:" & @TAB & DllStructGetData($tNMHEADER, "Button"))
					Return False ; To allow the header control to automatically manage drag-and-drop operations
;~ 						Return True  ; To indicate external (manual) drag-and-drop management allows the owner of the
					; control to provide custom services as part of the drag-and-drop process
				Case $HDN_BEGINTRACK, $HDN_BEGINTRACKW ; Notifies a header control's parent window that the user has begun dragging a divider in the control
					$tNMHEADER = DllStructCreate($tagNMHEADER, $ilParam)
					_DebugPrint("$HDN_BEGINTRACK" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMHEADER, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMHEADER, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMHEADER, "Code") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tNMHEADER, "Item") & @LF & _
							"-->Button:" & @TAB & DllStructGetData($tNMHEADER, "Button"))
					Return False ; To allow tracking of the divider
;~ 						Return True  ; To prevent tracking
				Case $HDN_DIVIDERDBLCLICK, $HDN_DIVIDERDBLCLICKW ; Notifies a header control's parent window that the user double-clicked the divider area of the control
					$tNMHEADER = DllStructCreate($tagNMHEADER, $ilParam)
					_DebugPrint("$HDN_DIVIDERDBLCLICK" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMHEADER, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMHEADER, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMHEADER, "Code") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tNMHEADER, "Item") & @LF & _
							"-->Button:" & @TAB & DllStructGetData($tNMHEADER, "Button"))
					; no return value
				Case $HDN_ENDDRAG ; Sent by a header control when a drag operation has ended on one of its items
					$tNMHEADER = DllStructCreate($tagNMHEADER, $ilParam)
					_DebugPrint("$HDN_ENDDRAG" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMHEADER, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMHEADER, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMHEADER, "Code") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tNMHEADER, "Item") & @LF & _
							"-->Button:" & @TAB & DllStructGetData($tNMHEADER, "Button"))
					Return False ; To allow the control to automatically place and reorder the item
;~ 						Return True  ; To prevent the item from being placed
				Case $HDN_ENDTRACK, $HDN_ENDTRACKW ; Notifies a header control's parent window that the user has finished dragging a divider
					$tNMHEADER = DllStructCreate($tagNMHEADER, $ilParam)
					_DebugPrint("$HDN_ENDTRACK" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMHEADER, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMHEADER, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMHEADER, "Code") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tNMHEADER, "Item") & @LF & _
							"-->Button:" & @TAB & DllStructGetData($tNMHEADER, "Button"))
					; no return value
				Case $HDN_FILTERBTNCLICK ; Notifies the header control's parent window when the filter button is clicked or in response to an $HDM_SETITEM message
					$tNMHDFILTERBTNCLICK = DllStructCreate($tagNMHDFILTERBTNCLICK, $ilParam)
					_DebugPrint("$HDN_FILTERBTNCLICK" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMHDFILTERBTNCLICK, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMHDFILTERBTNCLICK, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMHDFILTERBTNCLICK, "Code") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tNMHDFILTERBTNCLICK, "Item") & @LF & _
							"-->Left:" & @TAB & DllStructGetData($tNMHDFILTERBTNCLICK, "Left") & @LF & _
							"-->Top:" & @TAB & DllStructGetData($tNMHDFILTERBTNCLICK, "Top") & @LF & _
							"-->Right:" & @TAB & DllStructGetData($tNMHDFILTERBTNCLICK, "Right") & @LF & _
							"-->Bottom:" & @TAB & DllStructGetData($tNMHDFILTERBTNCLICK, "Bottom"))
;~ 						Return True  ; An $HDN_FILTERCHANGE notification will be sent to the header control's parent window
					; This notification gives the parent window an opportunity to synchronize its user interface elements
					Return False ; If you do not want the notification sent
				Case $HDN_FILTERCHANGE ; Notifies the header control's parent window that the attributes of a header control filter are being changed or edited
					$tNMHEADER = DllStructCreate($tagNMHEADER, $ilParam)
					_DebugPrint("$HDN_FILTERCHANGE" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMHEADER, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMHEADER, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMHEADER, "Code") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tNMHEADER, "Item") & @LF & _
							"-->Button:" & @TAB & DllStructGetData($tNMHEADER, "Button"))
					; no return value
				Case $HDN_GETDISPINFO, $HDN_GETDISPINFOW ; Sent to the owner of a header control when the control needs information about a callback header item
					$tNMHDDISPINFO = DllStructCreate($tagNMHDDISPINFO, $ilParam)
					_DebugPrint("$HDN_GETDISPINFO" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMHDDISPINFO, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMHDDISPINFO, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMHDDISPINFO, "Code") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tNMHDDISPINFO, "Item"))
;~ 						Return LRESULT
				Case $HDN_ITEMCHANGED, $HDN_ITEMCHANGEDW ; Notifies a header control's parent window that the attributes of a header item have changed
					$tNMHEADER = DllStructCreate($tagNMHEADER, $ilParam)
					_DebugPrint("$HDN_ITEMCHANGED" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMHEADER, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMHEADER, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMHEADER, "Code") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tNMHEADER, "Item") & @LF & _
							"-->Button:" & @TAB & DllStructGetData($tNMHEADER, "Button"))
					; no return value
				Case $HDN_ITEMCHANGING, $HDN_ITEMCHANGINGW ; Notifies a header control's parent window that the attributes of a header item are about to change
					$tNMHEADER = DllStructCreate($tagNMHEADER, $ilParam)
					_DebugPrint("$HDN_ITEMCHANGING" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMHEADER, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMHEADER, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMHEADER, "Code") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tNMHEADER, "Item") & @LF & _
							"-->Button:" & @TAB & DllStructGetData($tNMHEADER, "Button"))
					Return False ; To allow the changes
;~ 						Return True  ; To prevent them
				Case $HDN_ITEMCLICK, $HDN_ITEMCLICKW ; Notifies a header control's parent window that the user clicked the control
					$tNMHEADER = DllStructCreate($tagNMHEADER, $ilParam)
					_DebugPrint("$HDN_ITEMCLICK" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMHEADER, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMHEADER, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMHEADER, "Code") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tNMHEADER, "Item") & @LF & _
							"-->Button:" & @TAB & DllStructGetData($tNMHEADER, "Button"))
					; no return value
				Case $HDN_ITEMDBLCLICK, $HDN_ITEMDBLCLICKW ; Notifies a header control's parent window that the user double-clicked the control
					$tNMHEADER = DllStructCreate($tagNMHEADER, $ilParam)
					_DebugPrint("$HDN_ITEMDBLCLICK" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMHEADER, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMHEADER, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMHEADER, "Code") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tNMHEADER, "Item") & @LF & _
							"-->Button:" & @TAB & DllStructGetData($tNMHEADER, "Button"))
					; no return value
				Case $HDN_TRACK, $HDN_TRACKW ; Notifies a header control's parent window that the user is dragging a divider in the header control
					$tNMHEADER = DllStructCreate($tagNMHEADER, $ilParam)
					_DebugPrint("$HDN_TRACK" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMHEADER, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMHEADER, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMHEADER, "Code") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tNMHEADER, "Item") & @LF & _
							"-->Button:" & @TAB & DllStructGetData($tNMHEADER, "Button"))
					Return False ; To continue tracking the divider
;~ 						Return True  ; To end tracking
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
