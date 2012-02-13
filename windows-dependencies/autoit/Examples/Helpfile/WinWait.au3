;Wait for the window "[CLASS:Notepad]" to exist

Run("notepad.exe")
WinWait("[CLASS:Notepad]")

;Wait a maximum of 5 seconds for "[CLASS:Notepad]" to exist
WinWait("[CLASS:Notepad]", "", 5)
