#include <GUIConstantsEx.au3>
#include <GuiMonthCal.au3>
#include <WindowsConstants.au3>

$Debug_MC = False ; Check ClassName being passed to MonthCal functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hMonthCal

	; Create GUI
	GUICreate("Month Calendar Set Day State", 400, 300)
	$hMonthCal = GUICtrlCreateMonthCal("", 4, 4, -1, -1, BitOR($WS_BORDER, $MCS_DAYSTATE), 0x00000000)

	; Get the number of months that we must supply masks for.  Normally, this number will be 3.
	Local $aMasks[_GUICtrlMonthCal_GetMonthRangeSpan($hMonthCal, True)]

	; Make the 1st, 8th and the 16th of the current month bolded. This results in a binary mask of 1000 0000 1000 0001 or
	; 0x8081 in hex.
	$aMasks[1] = 0x8081
	_GUICtrlMonthCal_SetDayState($hMonthCal, $aMasks)

	GUISetState()

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
