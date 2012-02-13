;waits until no instance of notepad.exe exists
ProcessWaitClose("notepad.exe")

; This will wait until this particular instance of notepad has exited
Local $PID = Run("notepad.exe")
ProcessWaitClose($PID)
