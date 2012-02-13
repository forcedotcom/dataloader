If FileExists("C:\autoexec.bat") Then
	MsgBox(4096, "C:\autoexec.bat File", "Exists")
Else
	MsgBox(4096, "C:\autoexec.bat File", "Does NOT exists")
EndIf

If FileExists("C:\") Then
	MsgBox(4096, "C:\ Dir ", "Exists")
Else
	MsgBox(4096, "C:\ Dir", "Does NOT exists")
EndIf

If FileExists("D:") Then
	MsgBox(4096, "D: Drive", "Exists")
Else
	MsgBox(4096, "D: Drive", "Does NOT exists")
EndIf
