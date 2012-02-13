#include <GUIConstantsEx.au3>
#include <Date.au3>
#include <WindowsConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $pFileTime1, $tFileTime1, $pFileTime2, $tFileTime2

	; Create GUI
	GUICreate("Time", 400, 300)
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 296, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Compare FAT Dates/Times
	$tFileTime1 = _Date_Time_DOSDateTimeToFileTime(0x3621, 0x11EF) ; 01/01/2007 02:15:30
	$tFileTime2 = _Date_Time_DOSDateTimeToFileTime(0x379F, 0x944A) ; 12/31/2007 18:34:20
	$pFileTime1 = DllStructGetPtr($tFileTime1)
	$pFileTime2 = DllStructGetPtr($tFileTime2)

	MemoWrite("Result 1: " & _Date_Time_CompareFileTime($pFileTime1, $pFileTime2))
	MemoWrite("Result 2: " & _Date_Time_CompareFileTime($pFileTime1, $pFileTime1))
	MemoWrite("Result 3: " & _Date_Time_CompareFileTime($pFileTime2, $pFileTime1))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
