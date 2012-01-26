Local $file = FileOpen("test.txt", 1)

; Check if file opened for writing OK
If $file = -1 Then
	MsgBox(0, "Error", "Unable to open file.")
	Exit
EndIf

FileWrite($file, "Line1")
FileWrite($file, "Still Line1" & @CRLF)
FileWrite($file, "Line2")

FileClose($file)
