Run("notepad.exe")
WinWait("[CLASS:Notepad]")
ProcessSetPriority("notepad.exe", 0)
; Notepad should now have Idle/Low priority

