Run("notepad.exe")
WinWait("[CLASS:Notepad]")
ControlCommand("[CLASS:Notepad]", "", "Edit1", "GetLineCount", "")
