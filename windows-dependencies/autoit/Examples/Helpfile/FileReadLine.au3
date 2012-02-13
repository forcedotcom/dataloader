Local $file = FileOpen("test.txt", 0)

; Check if file opened for reading OK
If $file = -1 Then
	MsgBox(0, "Error", "Unable to open file.")
	Exit
EndIf

; Read in lines of text until the EOF is reached
While 1
	Local $line = FileReadLine($file)
	If @error = -1 Then ExitLoop
	MsgBox(0, "Line read:", $line)
WEnd

FileClose($file)
