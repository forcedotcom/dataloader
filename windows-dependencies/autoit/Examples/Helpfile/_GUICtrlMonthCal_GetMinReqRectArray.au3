#include <GUIConstantsEx.au3>
#include <GuiMonthCal.au3>
#include <EditConstants.au3>
#include <WindowsConstants.au3>

$Debug_MC = False ; Check ClassName being passed to MonthCal functions, set to True and use a handle to another control to see it work

Global $iMemo

_Main()

Func _Main()
	Local $hMonthCal

	; Create GUI
	GUICreate("Month Calendar Get Min Req Rect Array", 400, 300)
	$hMonthCal = GUICtrlCreateMonthCal("", 4, 4, -1, -1, BitOR($WS_BORDER, $MCS_MULTISELECT), 0x00000000)

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 4, 168, 392, 128, BitOR($WS_VSCROLL, $ES_MULTILINE))
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUICtrlSendMsg($iMemo, $EM_SETREADONLY, True, 0)
	GUICtrlSetBkColor($iMemo, 0xFFFFFF)
	GUISetState()

	; Get minimum required height/width
	MemoWrite(_FormatOutPut(_GUICtrlMonthCal_GetMinReqRectArray($hMonthCal)))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

Func _FormatOutPut($aRect)
	Return "Minimum required Width: " & @TAB & $aRect[3] & @CRLF & "Minimum required Height:" & @TAB & $aRect[4]
EndFunc   ;==>_FormatOutPut

; Write message to memo
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
