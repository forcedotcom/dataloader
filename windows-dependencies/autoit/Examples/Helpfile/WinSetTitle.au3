Run("notepad.exe")
WinWait("[CLASS:Notepad]")
WinSetTitle("[CLASS:Notepad]", "", "My New Notepad")
