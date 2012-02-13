Local $hDownload = InetGet("http://www.autoitscript.com/autoit3/files/beta/update.dat", @TempDir & "\update.dat", 1, 1)
Do
	Sleep(250)
Until InetGetInfo($hDownload, 2) ; Check if the download is complete.
Local $aData = InetGetInfo($hDownload) ; Get all information.
InetClose($hDownload) ; Close the handle to release resources.
MsgBox(0, "", "Bytes read: " & $aData[0] & @CRLF & _
		"Size: " & $aData[1] & @CRLF & _
		"Complete?: " & $aData[2] & @CRLF & _
		"Successful?: " & $aData[3] & @CRLF & _
		"@error: " & $aData[4] & @CRLF & _
		"@extended: " & $aData[5] & @CRLF)
