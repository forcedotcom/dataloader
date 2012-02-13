Sleep(2000) ;allow time to move mouse before reporting ID

;create an array that tells us the meaning of an ID Number
Local $IDs = StringSplit("AppStarting|Arrow|Cross|Help|IBeam|Icon|No|" & _
		"Size|SizeAll|SizeNESW|SizeNS|SizeNWSE|SizeWE|UpArrow|Wait|Hand", "|")
$IDs[0] = "Unknown"

Local $cursor = MouseGetCursor()
MsgBox(4096, "ID = " & $cursor, "Which means " & $IDs[$cursor])
