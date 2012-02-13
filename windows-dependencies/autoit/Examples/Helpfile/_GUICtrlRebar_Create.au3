#include <GuiReBar.au3>
#include <GuiToolbar.au3>
#include <GuiComboBox.au3>
#include <GuiDateTimePicker.au3>
#include <GuiEdit.au3>
#include <WindowsConstants.au3>
#include <Constants.au3>
#include <GUIConstantsEx.au3>

$Debug_RB = False

Global $hReBar

_Main()

Func _Main()
	Local $hgui, $btnExit, $hToolbar, $hCombo, $hDTP, $hInput
	Local Enum $idNew = 1000, $idOpen, $idSave, $idHelp

	$hgui = GUICreate("Rebar", 400, 396, -1, -1, BitOR($WS_MINIMIZEBOX, $WS_CAPTION, $WS_POPUP, $WS_SYSMENU, $WS_MAXIMIZEBOX))

	GUIRegisterMsg($WM_NOTIFY, "WM_NOTIFY")

	; create the rebar control
	$hReBar = _GUICtrlRebar_Create($hgui, BitOR($CCS_TOP, $WS_BORDER, $RBS_VARHEIGHT, $RBS_AUTOSIZE, $RBS_BANDBORDERS))


	; create a toolbar to put in the rebar
	$hToolbar = _GUICtrlToolbar_Create($hgui, BitOR($TBSTYLE_FLAT, $CCS_NORESIZE, $CCS_NOPARENTALIGN))

	; Add standard system bitmaps
	Switch _GUICtrlToolbar_GetBitmapFlags($hToolbar)
		Case 0
			_GUICtrlToolbar_AddBitmap($hToolbar, 1, -1, $IDB_STD_SMALL_COLOR)
		Case 2
			_GUICtrlToolbar_AddBitmap($hToolbar, 1, -1, $IDB_STD_LARGE_COLOR)
	EndSwitch

	; Add buttons
	_GUICtrlToolbar_AddButton($hToolbar, $idNew, $STD_FILENEW)
	_GUICtrlToolbar_AddButton($hToolbar, $idOpen, $STD_FILEOPEN)
	_GUICtrlToolbar_AddButton($hToolbar, $idSave, $STD_FILESAVE)
	_GUICtrlToolbar_AddButtonSep($hToolbar)
	_GUICtrlToolbar_AddButton($hToolbar, $idHelp, $STD_HELP)

	; create a combobox to put in the rebar
	$hCombo = _GUICtrlComboBox_Create($hgui, "", 0, 0, 120)

	_GUICtrlComboBox_BeginUpdate($hCombo)
	_GUICtrlComboBox_AddDir($hCombo, @WindowsDir & "\*.exe")
	_GUICtrlComboBox_EndUpdate($hCombo)

	; create a date time picker to put in the rebar
	$hDTP = _GUICtrlDTP_Create($hgui, 0, 0, 190)

	; create a input box to put in the rebar
;~ 	$hInput = GUICtrlCreateInput("Input control", 0, 0, 120, 20)
	$hInput = _GUICtrlEdit_Create($hgui, "Input control", 0, 0, 120, 20)


	; default for add is append

	; add band with control
	_GUICtrlRebar_AddBand($hReBar, $hCombo, 120, 200, "Dir *.exe")

	; add band with date time picker
	_GUICtrlRebar_AddBand($hReBar, $hDTP, 120)

	; add band with toolbar to begining of rebar
	_GUICtrlRebar_AddToolBarBand($hReBar, $hToolbar, "", 0)

	;add another control
;~ 	_GUICtrlReBar_AddBand($hReBar, GUICtrlGetHandle($hInput), 120, 200, "Name:")
	_GUICtrlRebar_AddBand($hReBar, $hInput, 120, 200, "Name:")


	$btnExit = GUICtrlCreateButton("Exit", 150, 360, 100, 25)
	GUISetState(@SW_SHOW)

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE, $btnExit
				Exit
		EndSwitch
	WEnd
EndFunc   ;==>_Main

Func WM_NOTIFY($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg, $iwParam
	Local $hWndFrom, $iIDFrom, $iCode, $tNMHDR
	Local $tAUTOBREAK, $tAUTOSIZE, $tNMREBAR, $tCHEVRON, $tCHILDSIZE, $tOBJECTNOTIFY

	$tNMHDR = DllStructCreate($tagNMHDR, $ilParam)
	$hWndFrom = HWnd(DllStructGetData($tNMHDR, "hWndFrom"))
	$iIDFrom = DllStructGetData($tNMHDR, "IDFrom")
	$iCode = DllStructGetData($tNMHDR, "Code")
	Switch $hWndFrom
		Case $hReBar
			Switch $iCode
				Case $RBN_AUTOBREAK
					; Notifies a rebar's parent that a break will appear in the bar. The parent determines whether to make the break
					$tAUTOBREAK = DllStructCreate($tagNMREBARAUTOBREAK, $ilParam)
					_DebugPrint("$RBN_AUTOBREAK" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tAUTOBREAK, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tAUTOBREAK, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tAUTOBREAK, "Code") & @LF & _
							"-->uBand:" & @TAB & DllStructGetData($tAUTOBREAK, "uBand") & @LF & _
							"-->wID:" & @TAB & DllStructGetData($tAUTOBREAK, "wID") & @LF & _
							"-->lParam:" & @TAB & DllStructGetData($tAUTOBREAK, "lParam") & @LF & _
							"-->uMsg:" & @TAB & DllStructGetData($tAUTOBREAK, "uMsg") & @LF & _
							"-->fStyleCurrent:" & @TAB & DllStructGetData($tAUTOBREAK, "fStyleCurrent") & @LF & _
							"-->fAutoBreak:" & @TAB & DllStructGetData($tAUTOBREAK, "fAutoBreak"))
					; Return value not used
				Case $RBN_AUTOSIZE
					; Sent by a rebar control created with the $RBS_AUTOSIZE style when the rebar automatically resizes itself
					$tAUTOSIZE = DllStructCreate($tagNMRBAUTOSIZE, $ilParam)
					_DebugPrint("$RBN_AUTOSIZE" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tAUTOSIZE, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tAUTOSIZE, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tAUTOSIZE, "Code") & @LF & _
							"-->fChanged:" & @TAB & DllStructGetData($tAUTOSIZE, "fChanged") & @LF & _
							"-->TargetLeft:" & @TAB & DllStructGetData($tAUTOSIZE, "TargetLeft") & @LF & _
							"-->TargetTop:" & @TAB & DllStructGetData($tAUTOSIZE, "TargetTop") & @LF & _
							"-->TargetRight:" & @TAB & DllStructGetData($tAUTOSIZE, "TargetRight") & @LF & _
							"-->TargetBottom:" & @TAB & DllStructGetData($tAUTOSIZE, "TargetBottom") & @LF & _
							"-->ActualLeft:" & @TAB & DllStructGetData($tAUTOSIZE, "ActualLeft") & @LF & _
							"-->ActualTop:" & @TAB & DllStructGetData($tAUTOSIZE, "ActualTop") & @LF & _
							"-->ActualRight:" & @TAB & DllStructGetData($tAUTOSIZE, "ActualRight") & @LF & _
							"-->ActualBottom:" & @TAB & DllStructGetData($tAUTOSIZE, "ActualBottom"))
					; Return value not used
				Case $RBN_BEGINDRAG
					; Sent by a rebar control when the user begins dragging a band
					$tNMREBAR = DllStructCreate($tagNMREBAR, $ilParam)
					_DebugPrint("$RBN_BEGINDRAG" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMREBAR, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMREBAR, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMREBAR, "Code") & @LF & _
							"-->dwMask:" & @TAB & DllStructGetData($tNMREBAR, "dwMask") & @LF & _
							"-->uBand:" & @TAB & DllStructGetData($tNMREBAR, "uBand") & @LF & _
							"-->fStyle:" & @TAB & DllStructGetData($tNMREBAR, "fStyle") & @LF & _
							"-->wID:" & @TAB & DllStructGetData($tNMREBAR, "wID") & @LF & _
							"-->lParam:" & @TAB & DllStructGetData($tNMREBAR, "lParam"))
					Return 0 ; to allow the rebar to continue the drag operation
;~ 					Return 1 ; nonzero to abort the drag operation
				Case $RBN_CHEVRONPUSHED
					; Sent by a rebar control when a chevron is pushed
					; When an application receives this notification, it is responsible for displaying a popup menu with items for each hidden tool.
					; Use the rc member of the NMREBARCHEVRON structure to find the correct position for the popup menu
					$tCHEVRON = DllStructCreate($tagNMREBARCHEVRON, $ilParam)
					_DebugPrint("$RBN_CHEVRONPUSHED" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tCHEVRON, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tCHEVRON, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tCHEVRON, "Code") & @LF & _
							"-->uBand:" & @TAB & DllStructGetData($tCHEVRON, "uBand") & @LF & _
							"-->wID:" & @TAB & DllStructGetData($tCHEVRON, "wID") & @LF & _
							"-->lParam:" & @TAB & DllStructGetData($tCHEVRON, "lParam") & @LF & _
							"-->Left:" & @TAB & DllStructGetData($tCHEVRON, "Left") & @LF & _
							"-->Top:" & @TAB & DllStructGetData($tCHEVRON, "Top") & @LF & _
							"-->Right:" & @TAB & DllStructGetData($tCHEVRON, "Right") & @LF & _
							"-->lParamNM:" & @TAB & DllStructGetData($tCHEVRON, "lParamNM"))
					; Return value not used
				Case $RBN_CHILDSIZE
					; Sent by a rebar control when a band's child window is resized
					$tCHILDSIZE = DllStructCreate($tagNMREBARCHILDSIZE, $ilParam)
					_DebugPrint("$RBN_CHILDSIZE" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tCHILDSIZE, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tCHILDSIZE, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tCHILDSIZE, "Code") & @LF & _
							"-->uBand:" & @TAB & DllStructGetData($tCHILDSIZE, "uBand") & @LF & _
							"-->wID:" & @TAB & DllStructGetData($tCHILDSIZE, "wID") & @LF & _
							"-->CLeft:" & @TAB & DllStructGetData($tCHILDSIZE, "CLeft") & @LF & _
							"-->CTop:" & @TAB & DllStructGetData($tCHILDSIZE, "CTop") & @LF & _
							"-->CRight:" & @TAB & DllStructGetData($tCHILDSIZE, "CRight") & @LF & _
							"-->CBottom:" & @TAB & DllStructGetData($tCHILDSIZE, "CBottom") & @LF & _
							"-->BLeft:" & @TAB & DllStructGetData($tCHILDSIZE, "BandLeft") & @LF & _
							"-->BTop:" & @TAB & DllStructGetData($tCHILDSIZE, "BTop") & @LF & _
							"-->BRight:" & @TAB & DllStructGetData($tCHILDSIZE, "BRight") & @LF & _
							"-->BBottom:" & @TAB & DllStructGetData($tCHILDSIZE, "BBottom"))
					; Return value not used
				Case $RBN_DELETEDBAND
					; Sent by a rebar control after a band has been deleted
					$tNMREBAR = DllStructCreate($tagNMREBAR, $ilParam)
					_DebugPrint("$RBN_DELETEDBAND" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMREBAR, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMREBAR, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMREBAR, "Code") & @LF & _
							"-->dwMask:" & @TAB & DllStructGetData($tNMREBAR, "dwMask") & @LF & _
							"-->uBand:" & @TAB & DllStructGetData($tNMREBAR, "uBand") & @LF & _
							"-->fStyle:" & @TAB & DllStructGetData($tNMREBAR, "fStyle") & @LF & _
							"-->wID:" & @TAB & DllStructGetData($tNMREBAR, "wID") & @LF & _
							"-->lParam:" & @TAB & DllStructGetData($tNMREBAR, "lParam"))
					; Return value not used
				Case $RBN_DELETINGBAND
					; Sent by a rebar control when a band is about to be deleted
					$tNMREBAR = DllStructCreate($tagNMREBAR, $ilParam)
					_DebugPrint("$RBN_DELETINGBAND" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMREBAR, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMREBAR, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMREBAR, "Code") & @LF & _
							"-->dwMask:" & @TAB & DllStructGetData($tNMREBAR, "dwMask") & @LF & _
							"-->uBand:" & @TAB & DllStructGetData($tNMREBAR, "uBand") & @LF & _
							"-->fStyle:" & @TAB & DllStructGetData($tNMREBAR, "fStyle") & @LF & _
							"-->wID:" & @TAB & DllStructGetData($tNMREBAR, "wID") & @LF & _
							"-->lParam:" & @TAB & DllStructGetData($tNMREBAR, "lParam"))
					; Return value not used
				Case $RBN_ENDDRAG
					; Sent by a rebar control when the user stops dragging a band
					$tNMREBAR = DllStructCreate($tagNMREBAR, $ilParam)
					_DebugPrint("$RBN_ENDDRAG" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tNMREBAR, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tNMREBAR, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tNMREBAR, "Code") & @LF & _
							"-->dwMask:" & @TAB & DllStructGetData($tNMREBAR, "dwMask") & @LF & _
							"-->uBand:" & @TAB & DllStructGetData($tNMREBAR, "uBand") & @LF & _
							"-->fStyle:" & @TAB & DllStructGetData($tNMREBAR, "fStyle") & @LF & _
							"-->wID:" & @TAB & DllStructGetData($tNMREBAR, "wID") & @LF & _
							"-->lParam:" & @TAB & DllStructGetData($tNMREBAR, "lParam"))
					; Return value not used
				Case $RBN_GETOBJECT
					; Sent by a rebar control created with the $RBS_REGISTERDROP style when an object is dragged over a band in the control
					$tOBJECTNOTIFY = DllStructCreate($tagNMOBJECTNOTIFY, $ilParam)
					_DebugPrint("$RBN_GETOBJECT" & @LF & "--> hWndFrom:" & @TAB & DllStructGetData($tOBJECTNOTIFY, "hWndFrom") & @LF & _
							"-->IDFrom:" & @TAB & DllStructGetData($tOBJECTNOTIFY, "IDFrom") & @LF & _
							"-->Code:" & @TAB & DllStructGetData($tOBJECTNOTIFY, "Code") & @LF & _
							"-->Item:" & @TAB & DllStructGetData($tOBJECTNOTIFY, "Item") & @LF & _
							"-->piid:" & @TAB & DllStructGetData($tOBJECTNOTIFY, "piid") & @LF & _
							"-->pObject:" & @TAB & DllStructGetData($tOBJECTNOTIFY, "pObject") & @LF & _
							"-->Result:" & @TAB & DllStructGetData($tOBJECTNOTIFY, "Result"))
					; Return value not used
				Case $RBN_HEIGHTCHANGE
					; Sent by a rebar control when its height has changed
					; Rebar controls that use the $CCS_VERT style send this notification message when their width changes
					_DebugPrint("$RBN_HEIGHTCHANGE" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; Return value not used
				Case $RBN_LAYOUTCHANGED
					; Sent by a rebar control when the user changes the layout of the control's bands
					_DebugPrint("$RBN_LAYOUTCHANGED" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; Return value not used
				Case $RBN_MINMAX
					; Sent by a rebar control prior to maximizing or minimizing a band
					_DebugPrint("$RBN_MINMAX" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
;~ 					Return 1 ; a non-zero value to prevent the operation from taking place
					Return 0 ; zero to allow it to continue
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
