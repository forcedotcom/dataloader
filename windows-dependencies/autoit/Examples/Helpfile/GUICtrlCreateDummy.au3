#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $user, $button, $cancel, $msg

	GUICreate("GUICtrlCreateDummy", 250, 200, 100, 200)
	GUISetBkColor(0x00E0FFFF) ; will change background color

	$user = GUICtrlCreateDummy()
	$button = GUICtrlCreateButton("event", 75, 170, 70, 20)
	$cancel = GUICtrlCreateButton("Cancel", 150, 170, 70, 20)
	GUISetState()

	Do
		$msg = GUIGetMsg()

		Select
			Case $msg = $button
				GUICtrlSendToDummy($user)
			Case $msg = $cancel
				GUICtrlSendToDummy($user)
			Case $msg = $user
				; special action before closing
				; ...
				Exit
		EndSelect
	Until $msg = $GUI_EVENT_CLOSE
EndFunc   ;==>Example
