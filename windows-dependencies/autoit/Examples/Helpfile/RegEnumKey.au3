For $i = 1 To 10
	Local $var = RegEnumKey("HKEY_LOCAL_MACHINE\SOFTWARE", $i)
	If @error <> 0 Then ExitLoop
	MsgBox(4096, "SubKey #" & $i & " under HKLM\Software: ", $var)
Next

