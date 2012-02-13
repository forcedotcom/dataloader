#include <GUIConstantsEx.au3>
#include <NetShare.au3>
#include <WindowsConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $sServer, $aInfo

	; Create GUI
	GUICreate("NetShare", 400, 300)

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 296, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Get server and share information
	$sServer = InputBox("NetWork Demo", "Enter Server Name:", "\\MyServer", "", 200, 130)
	If @error Then Exit

	; Enumerate network sessions
	$aInfo = _Net_Share_SessionEnum($sServer, @ComputerName)
	MemoWrite("Error ..........: " & @error)
	MemoWrite("Entries read ...: " & $aInfo[0][0])
	For $iI = 1 To $aInfo[0][0]
		MemoWrite("Computer name ..: " & $aInfo[$iI][0])
		MemoWrite("User name.......: " & $aInfo[$iI][1])
		MemoWrite("Resources open .: " & $aInfo[$iI][2])
		MemoWrite("Seconds active .: " & $aInfo[$iI][3])
		MemoWrite("Seconds idle ...: " & $aInfo[$iI][4])
		MemoWrite("Connection type : " & $aInfo[$iI][5])
		MemoWrite("Client type ....: " & $aInfo[$iI][6])
		MemoWrite("Transport ......: " & $aInfo[$iI][7])
		MemoWrite()
	Next

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Write message to memo
Func MemoWrite($sMessage = "")
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
