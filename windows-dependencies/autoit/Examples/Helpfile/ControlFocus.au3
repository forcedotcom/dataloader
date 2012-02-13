Run("notepad.exe")
WinWait("[CLASS:Notepad]")
ControlFocus("[CLASS:Notepad]", "", "Edit1")
