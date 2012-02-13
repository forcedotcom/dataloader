#include <GUIConstantsEx.au3>
#include <Date.au3>
#include <WindowsConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $aDate

	; Create GUI
	GUICreate("Time", 400, 300)
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 296, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Show FAT date/time
	$aDate = _Date_Time_DOSDateTimeToArray(0x3621, 0x944a) ; 01/01/2007 18:34:20
	MemoWrite("FAT date .: " & StringFormat("%02d/%02d/%04d", $aDate[0], $aDate[1], $aDate[2]))
	MemoWrite("FAT time .: " & StringFormat("%02d:%02d:%02d", $aDate[3], $aDate[4], $aDate[5]))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
