Local $val = ShellExecuteWait("notepad.exe")

; script waits until Notepad closes
MsgBox(0, "Program returned with exit code:", $val)
