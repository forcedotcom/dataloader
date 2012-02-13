#include <GUIConstantsEx.au3>
#include <GuiIPAddress.au3>
#include <WindowsConstants.au3>

$Debug_IP = False ; Check ClassName being passed to IPAddress functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hgui, $hIPAddress

	$hgui = GUICreate("IP Address Destroy Control Example", 400, 300)
	$hIPAddress = _GUICtrlIpAddress_Create($hgui, 10, 10, 125, 30, $WS_THICKFRAME)
	GUISetState(@SW_SHOW)

	; Clear the IP Address
	MsgBox(4160, "Information", "Destroy IP Address Control")
	_GUICtrlIpAddress_Destroy($hIPAddress)

	; Wait for user to close GUI
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main
