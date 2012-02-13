; Identify the Notepad window that contains the text "this one" and get a handle to it

; Get the handle of a notepad window that contains "this one"
Local $handle = WinGetHandle("[CLASS:Notepad]", "this one")
If @error Then
	MsgBox(4096, "Error", "Could not find the correct window")
Else
	; Send some text directly to this window's edit control
	ControlSend($handle, "", "Edit1", "AbCdE")
EndIf
