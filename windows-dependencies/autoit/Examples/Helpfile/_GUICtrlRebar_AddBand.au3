#include <GuiReBar.au3>
#include <GuiComboBox.au3>
#include <GuiDateTimePicker.au3>
#include <WindowsConstants.au3>
#include <Constants.au3>
#include <GUIConstantsEx.au3>

$Debug_RB = False

_Main()

Func _Main()
	Local $hgui, $btnExit, $hCombo, $hReBar, $hDTP, $hInput

	$hgui = GUICreate("Rebar", 400, 396, -1, -1, BitOR($WS_MINIMIZEBOX, $WS_CAPTION, $WS_POPUP, $WS_SYSMENU, $WS_MAXIMIZEBOX))

	; create the rebar control
	$hReBar = _GUICtrlRebar_Create($hgui, BitOR($CCS_TOP, $WS_BORDER, $RBS_VARHEIGHT, $RBS_AUTOSIZE, $RBS_BANDBORDERS))

	; create a combobox to put in the rebar
	$hCombo = _GUICtrlComboBox_Create($hgui, "", 0, 0, 120)

	_GUICtrlComboBox_BeginUpdate($hCombo)
	_GUICtrlComboBox_AddDir($hCombo, @WindowsDir & "\*.exe")
	_GUICtrlComboBox_EndUpdate($hCombo)

	; create a date time picker to put in the rebar
	$hDTP = _GUICtrlDTP_Create($hgui, 0, 0, 190)

	; create a input box to put in the rebar
	$hInput = GUICtrlCreateInput("Input control", 0, 0, 120, 20)

	; add band with control
	_GUICtrlRebar_AddBand($hReBar, $hCombo, 120, 200, "Dir *.exe")

	; add and force to second row
	_GUICtrlRebar_AddBand($hReBar, $hDTP, 120)
	_GUICtrlRebar_SetBandStyleBreak($hReBar, 1)

	; add to begining of rebar
	_GUICtrlRebar_AddBand($hReBar, GUICtrlGetHandle($hInput), 120, 200, "Name:", 0)

	$btnExit = GUICtrlCreateButton("Exit", 150, 360, 100, 25)
	GUISetState(@SW_SHOW)

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE, $btnExit
				Exit
		EndSwitch
	WEnd
EndFunc   ;==>_Main
