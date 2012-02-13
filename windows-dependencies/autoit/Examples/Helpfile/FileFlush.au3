Local Const $sFile = "test.txt"
Local $hFile = FileOpen($sFile, 1)

; Check if file opened for writing OK
If $hFile = -1 Then
	MsgBox(0, "Error", "Unable to open file.")
	Exit
EndIf

; Write something to the file.
FileWriteLine($hFile, "Line1")

; Run notepad to show that the file is empty because it hasn't been flushed yet.
RunWait("notepad.exe " & $sFile)

; Flush the file to disk.
FileFlush($hFile)

; Run notepad again to show that the contents of the file are now flushed to disk.
RunWait("notepad.exe " & $sFile)

; Close the handle.
FileClose($hFile)

; Clean up the temporary file.
FileDelete($sFile)

