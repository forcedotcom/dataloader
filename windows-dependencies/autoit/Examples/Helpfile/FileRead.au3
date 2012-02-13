Local $file = FileOpen("test.txt", 0)

; Check if file opened for reading OK
If $file = -1 Then
	MsgBox(0, "Error", "Unable to open file.")
	Exit
EndIf

; Read in 1 character at a time until the EOF is reached
While 1
	Local $chars = FileRead($file, 1)
	If @error = -1 Then ExitLoop
	MsgBox(0, "Char read:", $chars)
WEnd

FileClose($file)
