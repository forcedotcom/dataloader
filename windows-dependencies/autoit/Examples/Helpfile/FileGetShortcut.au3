; Create a shortcut on the desktop to explorer.exe and set the hotkey combination Ctrl+Alt+T or in AutoIt ^!t.
FileCreateShortcut(@WindowsDir & "\explorer.exe", @DesktopDir & "\Shortcut Example.lnk", @WindowsDir, "/e,c:\", "Tooltip description of the shortcut.", @SystemDir & "\shell32.dll", "^!t", "15", @SW_MINIMIZE)

; Retrieve the details of the shortcut.
Local $aDetails = FileGetShortcut(@DesktopDir & "\Shortcut Example.lnk")
If Not @error Then
	MsgBox(0, "FileGetShortcut", "Path: " & $aDetails[0] & @CRLF & _
			"Working directory: " & $aDetails[1] & @CRLF & _
			"Arguments: " & $aDetails[2] & @CRLF & _
			"Description: " & $aDetails[3] & @CRLF & _
			"Icon filename: " & $aDetails[4] & @CRLF & _
			"Icon index: " & $aDetails[5] & @CRLF & _
			"Shortcut state: " & $aDetails[6] & @CRLF)
EndIf
