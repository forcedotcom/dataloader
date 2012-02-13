; A simple custom messagebox that uses the OnEvent mode

#include <GUIConstantsEx.au3>

Opt("GUIOnEventMode", 1)

Global $ExitID

_Main()

Func _Main()
	Local $YesID, $NoID

	GUICreate("Custom Msgbox", 210, 80)

	GUICtrlCreateLabel("Please click a button!", 10, 10)
	$YesID = GUICtrlCreateButton("Yes", 10, 50, 50, 20)
	GUICtrlSetOnEvent($YesID, "OnYes")
	$NoID = GUICtrlCreateButton("No", 80, 50, 50, 20)
	GUICtrlSetOnEvent($NoID, "OnNo")
	$ExitID = GUICtrlCreateButton("Exit", 150, 50, 50, 20)
	GUICtrlSetOnEvent($ExitID, "OnExit")

	GUISetOnEvent($GUI_EVENT_CLOSE, "OnExit")

	GUISetState() ; display the GUI

	While 1
		Sleep(1000)
	WEnd
EndFunc   ;==>_Main

;--------------- Functions ---------------
Func OnYes()
	MsgBox(0, "You clicked on", "Yes")
EndFunc   ;==>OnYes

Func OnNo()
	MsgBox(0, "You clicked on", "No")
EndFunc   ;==>OnNo

Func OnExit()
	If @GUI_CtrlId = $ExitID Then
		MsgBox(0, "You clicked on", "Exit")
	Else
		MsgBox(0, "You clicked on", "Close")
	EndIf

	Exit
EndFunc   ;==>OnExit
