#include <WinAPI.au3>
#include <GUIConstantsEx.au3>

_Main()

Func _Main()
	Local $hwnd, $msg, $btnFlash
	$hwnd = GUICreate("__WinAPI_FlashWindow Example", 200, 200)
	$btnFlash = GUICtrlCreateButton("Flash Window", 50, 85, 100, 30)
	GUISetState(@SW_SHOW)
	While 1
		$msg = GUIGetMsg()
		Select
			Case $msg = $GUI_EVENT_CLOSE
				Exit
			Case $msg = $btnFlash
				GUISetState(@SW_MINIMIZE)
				Sleep(1000)
				_WinAPI_FlashWindow($hwnd)
				Sleep(6000)
		EndSelect
	WEnd
EndFunc   ;==>_Main
