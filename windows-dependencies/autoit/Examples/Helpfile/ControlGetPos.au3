Run("notepad.exe")
WinWait("[CLASS:Notepad]")
Local $aPos = ControlGetPos("[CLASS:Notepad]", "", "Edit1")
MsgBox(0, "Window Stats:", "Position: " & $aPos[0] & "," & $aPos[1] & @CRLF & "Size: " & $aPos[2] & "," & $aPos[3])