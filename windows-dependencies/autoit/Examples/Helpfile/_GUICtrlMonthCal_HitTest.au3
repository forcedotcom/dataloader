#include <GUIConstantsEx.au3>
#include <GuiMonthCal.au3>
#include <WindowsConstants.au3>

$Debug_MC = False ; Check ClassName being passed to MonthCal functions, set to True and use a handle to another control to see it work

Global $hMonthCal, $iMemo, $hGUI

_Main()

Func _Main()

	; Create GUI
	$hGUI = GUICreate("Month Calendar Hit Test", 400, 300)
	$hMonthCal = GUICtrlCreateMonthCal("", 4, 4, -1, -1, $WS_BORDER, 0x00000000)

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 4, 168, 392, 128, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Loop until user exits
	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_MOUSEMOVE
				DoHitTest()
			Case $GUI_EVENT_CLOSE
				ExitLoop
		EndSwitch
	WEnd
	GUIDelete()
EndFunc   ;==>_Main

; Perform hit test
Func DoHitTest()
	Local $tHit

	$tHit = _GUICtrlMonthCal_HitTest($hMonthCal, _WinAPI_GetMousePosX(True, $hGUI), _WinAPI_GetMousePosY(True, $hGUI))
	If BitAND(DllStructGetData($tHit, "Hit"), $MCHT_CALENDARDATE) <> 0 Then
		MemoWrite("Date: " & StringFormat("%02d/%02d/%04d", DllStructGetData($tHit, "Month"), _
				DllStructGetData($tHit, "Day"), _
				DllStructGetData($tHit, "Year")))
	EndIf
EndFunc   ;==>DoHitTest

; Write message to memo
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
