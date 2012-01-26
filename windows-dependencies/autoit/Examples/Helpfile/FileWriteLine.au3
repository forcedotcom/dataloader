Local $file = FileOpen("test.txt", 1)

; Check if file opened for writing OK
If $file = -1 Then
	MsgBox(0, "Error", "Unable to open file.")
	Exit
EndIf

FileWriteLine($file, "Line1")
FileWriteLine($file, "Line2" & @CRLF)
FileWriteLine($file, "Line3")

FileClose($file)
