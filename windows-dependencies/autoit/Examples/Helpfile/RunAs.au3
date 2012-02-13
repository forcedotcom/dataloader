; Fill in the username and password appropriate for your system.
Local $sUserName = "Username"
Local $sPassword = "Password"

; Run a command prompt as the other user.
RunAs($sUserName, @ComputerName, $sPassword, 0, @ComSpec, @SystemDir)
