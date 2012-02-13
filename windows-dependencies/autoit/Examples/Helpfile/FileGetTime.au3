Local $t = FileGetTime(@WindowsDir & "\notepad.exe", 1)

If Not @error Then
	Local $yyyymd = $t[0] & "/" & $t[1] & "/" & $t[2]
	MsgBox(0, "Creation date of notepad.exe", $yyyymd)
EndIf
