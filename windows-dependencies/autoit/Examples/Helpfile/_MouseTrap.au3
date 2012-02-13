#include <GUIConstantsEx.au3>
#include <Misc.au3>

_Main()

Func _Main()
	Local $GUI, $coords[4]

	$GUI = GUICreate("Mouse Trap Example", 392, 323)

	GUISetState()

	While 1
		$coords = WinGetPos($GUI)
		_MouseTrap($coords[0], $coords[1], $coords[0] + $coords[2], $coords[1] + $coords[3])
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
			Case Else
				;;;
		EndSwitch
	WEnd
	_MouseTrap()
	Exit
EndFunc   ;==>_Main
