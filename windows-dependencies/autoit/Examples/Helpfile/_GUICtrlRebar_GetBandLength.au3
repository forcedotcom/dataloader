#include <GUIConstantsEx.au3>
#include <GuiReBar.au3>
#include <GuiToolbar.au3>
#include <GuiComboBox.au3>
#include <GuiEdit.au3>
#include <WindowsConstants.au3>
#include <Constants.au3>

$Debug_RB = False

Global $iMemo

_Main()

Func _Main()
	Local $hgui, $btnExit, $hReBar, $hToolbar, $hCombo, $hInput
	Local Enum $idNew = 1000, $idOpen, $idSave, $idHelp

	$hgui = GUICreate("Rebar", 400, 396, -1, -1, BitOR($WS_MINIMIZEBOX, $WS_CAPTION, $WS_POPUP, $WS_SYSMENU, $WS_MAXIMIZEBOX))

	; create the rebar control
;~ 	$hReBar = _GUICtrlReBar_Create($hgui, BitOR($CCS_TOP, $WS_BORDER, $RBS_VARHEIGHT, $RBS_AUTOSIZE, $RBS_BANDBORDERS))
	$hReBar = _GUICtrlRebar_Create($hgui, BitOR($CCS_TOP, $WS_BORDER, $RBS_VARHEIGHT, $RBS_BANDBORDERS))

	$iMemo = GUICtrlCreateEdit("", 2, 100, 396, 250, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 10, 400, 0, "Courier New")

	; create a combobox to put in the rebar
	$hCombo = _GUICtrlComboBox_Create($hgui, "", 0, 0, 90, 120)
	_GUICtrlComboBox_BeginUpdate($hCombo)
	_GUICtrlComboBox_AddDir($hCombo, @WindowsDir & "\*.exe")
	_GUICtrlComboBox_EndUpdate($hCombo)

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

	; create a input box to put in the rebar
	$hInput = GUICtrlCreateInput("Input control", 0, 0, 90, 20)

	; add band containing the control to the begining of rebar
	_GUICtrlRebar_AddToolBarBand($hReBar, $hToolbar)

	;add band containing the control
	_GUICtrlRebar_AddBand($hReBar, $hCombo, 100, 120, "Dir *.exe:")

	;add band containing the control
	_GUICtrlRebar_AddBand($hReBar, GUICtrlGetHandle($hInput), 100, 100, "Name:")

	_GUICtrlRebar_SetBandBackColor($hReBar, 1, Int(0x00008B))
	_GUICtrlRebar_SetBandForeColor($hReBar, 1, Int(0xFFFFFF))

	GUISetState(@SW_SHOW)

	For $x = 0 To _GUICtrlRebar_GetBandCount($hReBar) - 1
		MemoWrite("Band Index " & $x & @TAB & "Length: " & _GUICtrlRebar_GetBandLength($hReBar, $x))
	Next

	MemoWrite("============================================")

	MsgBox(4096, "Information", "Setting Band Width")

	_GUICtrlRebar_SetBandLength($hReBar, 0, 200)

	For $x = 0 To _GUICtrlRebar_GetBandCount($hReBar) - 1
		MemoWrite("Band Index " & $x & @TAB & "Length: " & _GUICtrlRebar_GetBandLength($hReBar, $x))
	Next

	$btnExit = GUICtrlCreateButton("Exit", 150, 360, 100, 25)
	GUICtrlSetState($btnExit, $GUI_DEFBUTTON)
	GUICtrlSetState($btnExit, $GUI_FOCUS)


	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE, $btnExit
				Exit
		EndSwitch
	WEnd
EndFunc   ;==>_Main

; Write message to memo
Func MemoWrite($sMessage = "")
	_GUICtrlEdit_AppendText($iMemo, $sMessage & @CRLF)
EndFunc   ;==>MemoWrite
