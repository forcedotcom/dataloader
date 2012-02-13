; Press Esc to terminate script, Pause/Break to "pause"

Global $Paused
HotKeySet("{PAUSE}", "TogglePause")
HotKeySet("{ESC}", "Terminate")
HotKeySet("+!d", "ShowMessage") ;Shift-Alt-d

;;;; Body of program would go here ;;;;
While 1
	Sleep(100)
WEnd
;;;;;;;;

Func TogglePause()
	$Paused = Not $Paused
	While $Paused
		Sleep(100)
		ToolTip('Script is "Paused"', 0, 0)
	WEnd
	ToolTip("")
EndFunc   ;==>TogglePause

Func Terminate()
	Exit 0
EndFunc   ;==>Terminate

Func ShowMessage()
	MsgBox(4096, "", "This is a message.")
EndFunc   ;==>ShowMessage
