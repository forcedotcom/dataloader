Example()

Func Example()
	; Run Notepad
	Run("notepad.exe")

	; Wait 10 seconds for the Notepad window to appear.
	Local $hWnd = WinWait("[CLASS:Notepad]", "", 10)

	; Flash the window 4 times with a break in between each one of 1/2 second.
	WinFlash($hWnd, "", 4, 500)
EndFunc   ;==>Example
