; Find a pure red pixel in the range 0,0-20,300
Local $coord = PixelSearch(0, 0, 20, 300, 0xFF0000)
If Not @error Then
	MsgBox(0, "X and Y are:", $coord[0] & "," & $coord[1])
EndIf


; Find a pure red pixel or a red pixel within 10 shades variations of pure red
$coord = PixelSearch(0, 0, 20, 300, 0xFF0000, 10)
If Not @error Then
	MsgBox(0, "X and Y are:", $coord[0] & "," & $coord[1])
EndIf
