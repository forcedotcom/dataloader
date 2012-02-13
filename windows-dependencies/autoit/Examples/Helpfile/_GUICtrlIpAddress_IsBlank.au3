#include <GUIConstantsEx.au3>
#include <GuiIPAddress.au3>

$Debug_IP = False ; Check ClassName being passed to IPAddress functions, set to True and use a handle to another control to see it work

Global $iMemo

_Main()

Func _Main()
	Local $hgui, $hIPAddress

	$hgui = GUICreate("IP Address Control IsBlank Example", 400, 300)
	$hIPAddress = _GUICtrlIpAddress_Create($hgui, 2, 4)
	$iMemo = GUICtrlCreateEdit("", 2, 28, 396, 270, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState(@SW_SHOW)

	; See if IP Address is blank
	MemoWrite("Blank: " & _GUICtrlIpAddress_IsBlank($hIPAddress))

	Sleep(1000)

	_GUICtrlIpAddress_Set($hIPAddress, "24.168.2.128")

	; See if IP Address is blank
	MemoWrite("Blank: " & _GUICtrlIpAddress_IsBlank($hIPAddress))

	; Wait for user to close GUI
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
