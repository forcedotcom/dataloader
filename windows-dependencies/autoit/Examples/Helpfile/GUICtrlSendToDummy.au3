#include <GUIConstantsEx.au3>

Global $user

Example()

Func Example()
	Local $iOldOpt
	$iOldOpt = Opt("GUIOnEventMode", 1)

	GUICreate("GUISendToDummy", 220, 200, 100, 200)
	GUISetBkColor(0x00E0FFFF) ; will change background color
	GUICtrlSetOnEvent($GUI_EVENT_CLOSE, "OnClick") ; to handle click on button

	$user = GUICtrlCreateDummy()
	GUICtrlSetOnEvent(-1, "Onexit") ; to handle click on button
	GUICtrlCreateButton("event", 75, 170, 70, 20)
	GUICtrlSetOnEvent(-1, "OnClick") ; to handle click on button
	GUISetState()

	While 1
		Sleep(100)
	WEnd

	Opt("GUIOnEventMode", $iOldOpt)

EndFunc   ;==>Example

Func OnClick()
	GUICtrlSendToDummy($user) ; fired dummy control
EndFunc   ;==>OnClick

Func OnExit()
	; special action before exiting
	Exit
EndFunc   ;==>OnExit
