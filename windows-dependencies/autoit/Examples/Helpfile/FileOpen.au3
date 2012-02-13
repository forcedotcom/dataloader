Local $file = FileOpen("test.txt", 0)

; Check if file opened for reading OK
If $file = -1 Then
	MsgBox(0, "Error", "Unable to open file.")
	Exit
EndIf

FileClose($file)


; Another sample which automatically creates the directory structure
$file = FileOpen("test.txt", 10) ; which is similar to 2 + 8 (erase + create dir)

If $file = -1 Then
	MsgBox(0, "Error", "Unable to open file.")
	Exit
EndIf

FileClose($file)
