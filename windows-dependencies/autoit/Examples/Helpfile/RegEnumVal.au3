For $i = 1 To 100
	Local $var = RegEnumVal("HKEY_LOCAL_MACHINE\SOFTWARE\AutoIt v3\AutoIt", $i)
	If @error <> 0 Then ExitLoop
	MsgBox(4096, "Value Name  #" & $i & " under in AutoIt3 key", $var)
Next
