Local $sFldr1 = "C:\Test1\"
Local $sFldr2 = "C:\Test1\Folder1\"
Local $sFldr3 = "C:\Test1\Folder1\Folder2\"
If DirGetSize($sFldr1) = -1 Then
	DirCreate($sFldr3)
	Local $explorer = RunWait("explorer /root, C:\Test1\Folder1")
	Local $handle = WinGetHandle($explorer)
	MsgBox(262144, "Message", "Explorer is opened with Folder2 displayed.")
	DirRemove($sFldr3, 1)
	MsgBox(262144, "Message", "The sub folder: Folder2 has been deleted.")
	WinClose($handle)
	DirRemove($sFldr2) ;clean up test folders
	DirRemove($sFldr1) ;clean up test folders
Else
	MsgBox(48, $sFldr1, "Directory already exists!")
EndIf
