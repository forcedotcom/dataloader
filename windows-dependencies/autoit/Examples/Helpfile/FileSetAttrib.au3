;mark all .au3 files in current directory as read-only and system
If Not FileSetAttrib("*.au3", "+RS") Then
	MsgBox(4096, "Error", "Problem setting attributes.")
EndIf

;make all .bmp files in C:\ and sub-directories writable and archived
If Not FileSetAttrib("C:\*.bmp", "-R+A", 1) Then
	MsgBox(4096, "Error", "Problem setting attributes.")
EndIf
