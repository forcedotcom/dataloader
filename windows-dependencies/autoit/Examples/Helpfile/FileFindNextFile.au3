; Shows the filenames of all files in the current directory
Local $search = FileFindFirstFile("*.*")

; Check if the search was successful
If $search = -1 Then
	MsgBox(0, "Error", "No files/directories matched the search pattern")
	Exit
EndIf

While 1
	Local $file = FileFindNextFile($search)
	If @error Then ExitLoop

	MsgBox(4096, "File:", $file)
WEnd

; Close the search handle
FileClose($search)
