#include <WinAPI.au3>
#include <GUIConstantsEx.au3>

_Main()

Func _Main()
	Local $hwnd, $Flash, $Timeout, $btnFlash, $msg, $flashrate, $timeoutrate, $flashing = False
	$hwnd = GUICreate("Form1", 229, 170, 193, 125)
	$Flash = GUICtrlCreateInput("20", 80, 72, 121, 21)
	$Timeout = GUICtrlCreateInput("500", 80, 103, 121, 21)
	GUICtrlCreateLabel("Please input the flash rate, and the time between flashes", 8, 24, 214, 41)
	GUICtrlCreateLabel("Flash Rate:", 16, 72, 58, 17)
	GUICtrlCreateLabel("Timeout (ms)", 16, 104, 64, 17)
	$btnFlash = GUICtrlCreateButton("Flash Window", 80, 136, 75, 25, 0)
	GUISetState(@SW_SHOW)
	#endregion

	While 1
		$msg = GUIGetMsg()
		Switch $msg
			Case $GUI_EVENT_CLOSE
				Exit
			Case $btnFlash
				If $flashing Then
					_WinAPI_FlashWindowEx($hwnd, 0)
					$flashing = False
				Else
					$flashrate = GUICtrlRead($Flash)
					$timeoutrate = GUICtrlRead($Timeout)
					_WinAPI_FlashWindowEx($hwnd, 2, $flashrate, $timeoutrate)
					GUICtrlSetData($btnFlash, "Stop Flashing")
					$flashing = True
				EndIf
		EndSwitch
	WEnd
EndFunc   ;==>_Main
