Example()

Func Example()
	; Run Notepad
	Run("notepad.exe")

	; Wait 10 seconds for the Notepad window to appear.
	Local $hWnd = WinWait("[CLASS:Notepad]", "", 10)

	; Retrieve the position and size of the Notepad window by passing the handle to WinGetPos.
	Local $aPos = WinGetPos($hWnd)

	; Check if the variable is an array.
	If IsArray($aPos) Then
		MsgBox(0, "Success", "Window height: " & $aPos[3])
	Else
		MsgBox(0, "Error", "An error occurred.")
	EndIf
EndFunc   ;==>Example
