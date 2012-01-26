#include <GUIConstantsEx.au3>
#include <WinAPI.au3>
#include <WindowsConstants.au3>

_Main()

Func _Main()
	Local $aElements[2] = [$COLOR_ACTIVECAPTION, $COLOR_GRADIENTACTIVECAPTION]
	; Red and Yellow
	Local $aColors[2] = [255, 65535], $aSaveColors[2]

	GUICreate("My GUI", 300, 200)

	$aSaveColors[0] = _WinAPI_GetSysColor($COLOR_ACTIVECAPTION)
	$aSaveColors[1] = _WinAPI_GetSysColor($COLOR_GRADIENTACTIVECAPTION)

	_WinAPI_SetSysColors($aElements, $aColors)

	GUISetState()

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop

		EndSwitch
	WEnd

	GUIDelete()

	_WinAPI_SetSysColors($aElements, $aSaveColors)

	Exit

EndFunc   ;==>_Main
