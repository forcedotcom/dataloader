#include <GUIConstantsEx.au3>
#include <GuiDateTimePicker.au3>
#include <Constants.au3>

$Debug_DTP = False ; Check ClassName being passed to DTP functions, set to True and use a handle to another control to see it work

Global $iMemo

_Main() ;use autoit built-in control

Func _Main()
	Local $hDTP

	; Create GUI
	GUICreate("DateTimePick Get Month Calendar Color", 400, 300)
	$hDTP = GUICtrlGetHandle(GUICtrlCreateDate("", 2, 6, 190))
	$iMemo = GUICtrlCreateEdit("", 2, 32, 396, 266, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Set the display format
	_GUICtrlDTP_SetFormat($hDTP, "ddd MMM dd, yyyy hh:mm ttt")

	; Set month calendar background color
	_GUICtrlDTP_SetMCColor($hDTP, 2, $CLR_MONEYGREEN)

	; Get DTP colors
	MemoWrite("Background between months: " & "0x" & Hex(_GUICtrlDTP_GetMCColor($hDTP, 0), 6))
	MemoWrite("Text within months ......: " & "0x" & Hex(_GUICtrlDTP_GetMCColor($hDTP, 1), 6))
	MemoWrite("Title background ........: " & "0x" & Hex(_GUICtrlDTP_GetMCColor($hDTP, 2), 6))
	MemoWrite("Title text ..............: " & "0x" & Hex(_GUICtrlDTP_GetMCColor($hDTP, 3), 6))
	MemoWrite("Background within months : " & "0x" & Hex(_GUICtrlDTP_GetMCColor($hDTP, 4), 6))
	MemoWrite("Header trailing .........: " & "0x" & Hex(_GUICtrlDTP_GetMCColor($hDTP, 5), 6))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
