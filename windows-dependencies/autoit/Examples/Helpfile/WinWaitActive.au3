;Wait for the window "[CLASS:Notepad]" to exist and be active
WinWaitActive("[CLASS:Notepad]")

;Wait a maximum of 5 seconds for "[CLASS:Notepad]" to exist and be active
WinWaitActive("[CLASS:Notepad]", "", 5)
