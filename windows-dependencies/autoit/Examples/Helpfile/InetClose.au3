Local $hDownload = InetGet("http://www.autoitscript.com/autoit3/files/beta/update.dat", @TempDir & "\update.dat", 1, 1)
Do
	Sleep(250)
Until InetGetInfo($hDownload, 2) ; Check if the download is complete.
Local $nBytes = InetGetInfo($hDownload, 0)
InetClose($hDownload) ; Close the handle to release resources.
MsgBox(0, "", "Bytes read: " & $nBytes)
