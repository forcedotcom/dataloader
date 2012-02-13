Local $var = IniReadSection("C:\Temp\myfile.ini", "section2")
If @error Then
	MsgBox(4096, "", "Error occurred, probably no INI file.")
Else
	For $i = 1 To $var[0][0]
		MsgBox(4096, "", "Key: " & $var[$i][0] & @CRLF & "Value: " & $var[$i][1])
	Next
EndIf
