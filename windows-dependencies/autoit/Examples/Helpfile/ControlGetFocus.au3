Run("notepad.exe")
WinWait("[CLASS:Notepad]")
Local $sControl = ControlGetFocus("[CLASS:Notepad]")
MsgBox(0, "ControlGetFocus Example", "The control that has focus is: " & $sControl)