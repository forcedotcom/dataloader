Run("notepad.exe")
WinWait("[CLASS:Notepad]")
Local $hHandle = ControlGetHandle("[CLASS:Notepad]", "", "Edit1")
MsgBox(0, "ControlGetHandle Example", "The control handle of Edit1 is: " & $hHandle)