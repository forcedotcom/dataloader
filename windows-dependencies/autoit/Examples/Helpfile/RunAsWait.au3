; Fill in the username and password appropriate for your system.
Local $sUserName = "Username"
Local $sPassword = "Password"

; Run a command prompt as the other user.
Local $pid = RunAsWait($sUserName, @ComputerName, $sPassword, 0, @ComSpec, @SystemDir)

; Wait for the process to close.
ProcessWaitClose($pid)

; Show a message.
MsgBox(0, "", "The process we were waiting for has closed.")
