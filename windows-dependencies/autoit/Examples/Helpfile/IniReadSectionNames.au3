Local $var = IniReadSectionNames(@WindowsDir & "\win.ini")
If @error Then
	MsgBox(4096, "", "Error occurred, probably no INI file.")
Else
	For $i = 1 To $var[0]
		MsgBox(4096, "", $var[$i])
	Next
EndIf
