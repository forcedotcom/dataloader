Example()

Func Example()
	; Run Notepad
	Run("notepad.exe")

	; Wait 10 seconds for the Notepad window to appear.
	Local $hWnd = WinWait("[CLASS:Notepad]", "", 10)

	; Test if the window was found and display the results.
	If IsHWnd($hWnd) Then
		MsgBox(4096, "", "It's a valid HWND")
	Else
		MsgBox(4096, "", "It's not a valid HWND")
	EndIf
EndFunc   ;==>Example
