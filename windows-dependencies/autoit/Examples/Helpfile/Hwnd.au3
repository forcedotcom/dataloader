Example()

Func Example()
	; Run Notepad
	Run("notepad.exe")

	; Wait 10 seconds for the Notepad window to appear.
	Local $hWnd = WinWait("[CLASS:Notepad]", "", 10)

	; Convert the handle to a string.
	Local $sHWnd = String($hWnd)

	; Minimize the Notepad window and wait for 2 seconds.
	WinSetState(HWnd($sHWnd), "", @SW_MINIMIZE)
	Sleep(2000)

	; Restore the Notepad window and wait for 2 seconds.
	WinSetState(HWnd($sHWnd), "", @SW_RESTORE)
	Sleep(2000)

	WinClose(HWnd($sHWnd)) ; Close the Notepad window.
EndFunc   ;==>Example
