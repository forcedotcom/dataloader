Local $aArray = DriveGetDrive("ALL")
If @error Then
	; An error occurred when retrieving the drives.
	MsgBox(4096, "DriveGetDrive", "It appears an error occurred.")
Else
	For $i = 1 To $aArray[0]
		; Show all the drives found and convert the drive letter to uppercase.
		MsgBox(4096, "DriveGetDrive", "Drive " & $i & "/" & $aArray[0] & ":" & @CRLF & StringUpper($aArray[$i]))
	Next
EndIf
