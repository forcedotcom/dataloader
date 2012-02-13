BlockInput(1)

Run("notepad.exe")
WinWaitActive("[CLASS:Notepad]")
Send("{F5}") ; Pastes the date and time

BlockInput(0)
