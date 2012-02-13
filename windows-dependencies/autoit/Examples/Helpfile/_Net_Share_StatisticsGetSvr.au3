#include <GUIConstantsEx.au3>
#include <NetShare.au3>
#include <WindowsConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $aInfo

	; Create GUI
	GUICreate("NetShare", 400, 300)

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 296, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Get server statistics
	$aInfo = _Net_Share_StatisticsGetSvr(@ComputerName)
	MemoWrite("Statistics started ......: " & $aInfo[0])
	MemoWrite("Times file opened .......: " & $aInfo[1])
	MemoWrite("Times device opened .....: " & $aInfo[2])
	MemoWrite("Print jobs spooled ......: " & $aInfo[3])
	MemoWrite("Sessions started ........: " & $aInfo[4])
	MemoWrite("Sessions disconnected ...: " & $aInfo[5])
	MemoWrite("Session errors ..........: " & $aInfo[6])
	MemoWrite("Password violations .....: " & $aInfo[7])
	MemoWrite("Permission errors .......: " & $aInfo[8])
	MemoWrite("Server system errors ....: " & $aInfo[9])
	MemoWrite("Network bytes sent ......: " & $aInfo[10])
	MemoWrite("Network bytes recv ......: " & $aInfo[11])
	MemoWrite("Average response time ...: " & $aInfo[12])
	MemoWrite("Req buffer failures .....: " & $aInfo[13])
	MemoWrite("Big buffer failures .....: " & $aInfo[14])

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Write message to memo
Func MemoWrite($sMessage = "")
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
