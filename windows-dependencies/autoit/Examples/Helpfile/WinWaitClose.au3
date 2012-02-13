;Wait for the window "[CLASS:Notepad]" to not exist
WinWaitClose("[CLASS:Notepad]")

;Wait a maximum of 5 seconds for "[CLASS:Notepad]" to not exist
WinWaitClose("[CLASS:Notepad]", "", 5)
