#include <GUIConstantsEx.au3>
#include <GuiMonthCal.au3>
#include <EditConstants.au3>
#include <WindowsConstants.au3>
#include <Constants.au3>

$Debug_MC = False ; Check ClassName being passed to MonthCal functions, set to True and use a handle to another control to see it work

Global $iMemo

_Main()

Func _Main()
	Local $hMonthCal

	; Create GUI
	GUICreate("Month Calendar Get Color Array", 425, 300)
	$hMonthCal = GUICtrlCreateMonthCal("", 4, 4, -1, -1, $WS_BORDER, 0x00000000)

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 4, 168, 417, 128, BitOR($WS_VSCROLL, $ES_MULTILINE))
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUICtrlSendMsg($iMemo, $EM_SETREADONLY, True, 0)
	GUICtrlSetBkColor($iMemo, 0xFFFFFF)
	GUISetState()

	_GUICtrlMonthCal_SetColor($hMonthCal, $MCSC_MONTHBK, $CLR_MONEYGREEN)

	; Get/Set calendar colors
	MemoWrite(_FormatOutPut("Background color displayed between months:", _GUICtrlMonthCal_GetColorArray($hMonthCal, $MCSC_BACKGROUND)))
	MemoWrite(_FormatOutPut(@CRLF & "Background color displayed within the month:", _GUICtrlMonthCal_GetColorArray($hMonthCal, $MCSC_MONTHBK)))
	MemoWrite(_FormatOutPut(@CRLF & "Color used to display text within a month:", _GUICtrlMonthCal_GetColorArray($hMonthCal, $MCSC_TEXT)))
	MemoWrite(_FormatOutPut(@CRLF & "Background color displayed in the calendar's title:", _GUICtrlMonthCal_GetColorArray($hMonthCal, $MCSC_TITLEBK)))
	MemoWrite(_FormatOutPut(@CRLF & "Color used to display text within the calendar's title:", _GUICtrlMonthCal_GetColorArray($hMonthCal, $MCSC_TITLETEXT)))
	MemoWrite(_FormatOutPut(@CRLF & "Color used to display header day and trailing day text:", _GUICtrlMonthCal_GetColorArray($hMonthCal, $MCSC_TRAILINGTEXT)))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

Func _FormatOutPut($sText, $aColors)
	Return $sText & _
			@CRLF & @TAB & "COLORREF rgbcolor:" & @TAB & $aColors[1] & _
			@CRLF & @TAB & "Hex BGR color:" & @TAB & @TAB & $aColors[2] & _
			@CRLF & @TAB & "Hex RGB color:" & @TAB & @TAB & $aColors[3]
EndFunc   ;==>_FormatOutPut

; Write message to memo
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
