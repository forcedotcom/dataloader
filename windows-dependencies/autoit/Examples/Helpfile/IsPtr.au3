Run("notepad.exe")
Local $hWnd = WinWait("[CLASS:Notepad]")
If IsPtr($hWnd) Then
	MsgBox(4096, "", "It's a valid Ptr")
Else
	MsgBox(4096, "", "It's not a valid Ptr")
EndIf
