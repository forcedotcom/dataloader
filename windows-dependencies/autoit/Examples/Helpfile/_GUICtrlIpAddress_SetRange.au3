#include <GUIConstantsEx.au3>
#include <GuiIPAddress.au3>

$Debug_IP = False ; Check ClassName being passed to IPAddress functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hgui, $hIPAddress

	$hgui = GUICreate("IP Address Control Set Range Example", 300, 150)
	$hIPAddress = _GUICtrlIpAddress_Create($hgui, 10, 10)
	GUISetState(@SW_SHOW)

	_GUICtrlIpAddress_Set($hIPAddress, "24.168.2.128")

	; set range on 1st field
	_GUICtrlIpAddress_SetRange($hIPAddress, 0, 198, 200)

	; Wait for user to close GUI
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main
