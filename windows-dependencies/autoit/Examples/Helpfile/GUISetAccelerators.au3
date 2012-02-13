; A simple custom messagebox that uses the MessageLoop mode

#include <GUIConstantsEx.au3>

GUICreate("Custom Msgbox", 210, 80)

GUICtrlCreateLabel("Please click a button!", 10, 10)
Local $YesID = GUICtrlCreateButton("Yes", 10, 50, 50, 20)
Local $NoID = GUICtrlCreateButton("No", 80, 50, 50, 20)
Local $ExitID = GUICtrlCreateButton("Exit", 150, 50, 50, 20)

; Set accelerators for Ctrl+y and Ctrl+n
Local $AccelKeys[2][2] = [["^y", $YesID],["^n", $NoID]]
GUISetAccelerators($AccelKeys)

GUISetState() ; display the GUI

Do
	Local $msg = GUIGetMsg()

	Select
		Case $msg = $YesID
			MsgBox(0, "You clicked on", "Yes")
		Case $msg = $NoID
			MsgBox(0, "You clicked on", "No")
		Case $msg = $ExitID
			MsgBox(0, "You clicked on", "Exit")
		Case $msg = $GUI_EVENT_CLOSE
			MsgBox(0, "You clicked on", "Close")
	EndSelect
Until $msg = $GUI_EVENT_CLOSE Or $msg = $ExitID
