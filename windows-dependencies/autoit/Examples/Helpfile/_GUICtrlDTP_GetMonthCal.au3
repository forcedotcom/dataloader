#include <GUIConstantsEx.au3>
#include <GuiDateTimePicker.au3>

$Debug_DTP = False ; Check ClassName being passed to DTP functions, set to True and use a handle to another control to see it work

Global $iMemo

_Main()

Func _Main()
	Local $hGui, $hDTP

	; Create GUI
	$hGui = GUICreate("DateTimePick Get Month Calendar Child Handle", 400, 300)
	$hDTP = _GUICtrlDTP_Create($hGui, 2, 6, 190)
	$iMemo = GUICtrlCreateEdit("", 2, 32, 396, 266, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Set the display format
	_GUICtrlDTP_SetFormat($hDTP, "ddd MMM dd, yyyy hh:mm ttt")

	; Get month control child handle
	GUICtrlSetData($iMemo, "MonthCal child Handle: " & "0x" & Hex(_GUICtrlDTP_GetMonthCal($hDTP)), 1)
	GUICtrlSetData($iMemo, " IsPtr=" & IsPtr(_GUICtrlDTP_GetMonthCal($hDTP)), 1)
	GUICtrlSetData($iMemo, " IsHwnd=" & IsHWnd(_GUICtrlDTP_GetMonthCal($hDTP)) & @CRLF, 1)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
