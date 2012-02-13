Run("notepad.exe")
WinWait("[CLASS:Notepad]")
Local $pid = WinGetProcess("[CLASS:Notepad]")
MsgBox(4096, "PID is", $pid)
