#include <GUIConstantsEx.au3>

GUICreate("GUI with simple context menu", 300, 200)

Local $trackmenu = GUICtrlCreateContextMenu()
Local $aboutitem = GUICtrlCreateMenuItem("About", $trackmenu)
; next one creates a menu separator (line)
GUICtrlCreateMenuItem("", $trackmenu)
Local $exititem = GUICtrlCreateMenuItem("Exit", $trackmenu)

GUISetState()

While 1
	Local $msg = GUIGetMsg()
	If $msg = $exititem Or $msg = $GUI_EVENT_CLOSE Or $msg = -1 Then ExitLoop
	If $msg = $aboutitem Then MsgBox(0, "About", "A simple example with a context menu!")
WEnd
