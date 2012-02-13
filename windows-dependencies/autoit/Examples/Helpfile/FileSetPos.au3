#include <Constants.au3>

Local Const $sFile = "test.txt"
Local $hFile = FileOpen($sFile, 2)

; Check if file opened for writing OK
If $hFile = -1 Then
	MsgBox(0, "Error", "Unable to open file.")
	Exit
EndIf

; Write something to the file.
FileWriteLine($hFile, "Line1")
FileWriteLine($hFile, "Line2")
FileWriteLine($hFile, "Line3")

; Flush the file to disk.
FileFlush($hFile)

; Check file position and try to read contents for current position.
MsgBox(0, "", "Position: " & FileGetPos($hFile) & @CRLF & "Data: " & @CRLF & FileRead($hFile))

; Now, adjust the position to the beginning.
Local $n = FileSetPos($hFile, 0, $FILE_BEGIN)

; Check file position and try to read contents for current position.
MsgBox(0, "", "Position: " & FileGetPos($hFile) & @CRLF & "Data: " & @CRLF & FileRead($hFile))

; Close the handle.
FileClose($hFile)

; Clean up the temporary file.
FileDelete($sFile)
