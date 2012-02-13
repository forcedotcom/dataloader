Local $size = DirGetSize(@HomeDrive)
MsgBox(0, "", "Size(MegaBytes):" & Round($size / 1024 / 1024))

$size = DirGetSize(@WindowsDir, 2)
MsgBox(0, "", "Size(MegaBytes):" & Round($size / 1024 / 1024))

Local $timer = TimerInit()
$size = DirGetSize("\\10.0.0.1\h$", 1)
Local $diff = Round(TimerDiff($timer) / 1000) ; time in seconds
If IsArray($size) Then
	MsgBox(0, "DirGetSize-Info", "Size(Bytes):" & $size[0] & @LF _
			 & "Files:" & $size[1] & @LF & "Dirs:" & $size[2] & @LF _
			 & "TimeDiff(Sec):" & $diff)
EndIf
