#include <GUIConstantsEx.au3>
#include <GuiIPAddress.au3>

$Debug_IP = False ; Check ClassName being passed to IPAddress functions, set to True and use a handle to another control to see it work

Global $iMemo

_Main()

Func _Main()
	Local $hgui, $tIP = DllStructCreate($tagGetIPAddress), $aIP[4] = [24, 168, 2, 128], $hIPAddress

	$hgui = GUICreate("IP Address Control GetEx Example", 400, 300)
	$hIPAddress = _GUICtrlIpAddress_Create($hgui, 2, 4, 125, 20)
	$iMemo = GUICtrlCreateEdit("", 2, 28, 396, 270, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState(@SW_SHOW)

	For $x = 0 To 3
		DllStructSetData($tIP, "Field" & $x + 1, $aIP[$x])
	Next

	_GUICtrlIpAddress_SetEx($hIPAddress, $tIP)

	$tIP = _GUICtrlIpAddress_GetEx($hIPAddress)

	For $x = 0 To 3
		MemoWrite("Field " & $x + 1 & " .....: " & DllStructGetData($tIP, "Field" & $x + 1))
	Next

	; Wait for user to close GUI
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
