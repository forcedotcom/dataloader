InetGet("http://www.mozilla.org", @TempDir & "\mozilla.html")
InetGet("http://www.autoitscript.com", @TempDir & "autoitscript.html", 1)
InetGet("ftp://ftp.mozilla.org/pub/mozilla.org/README", @TempDir & "\Mozilla-README.txt", 1)

; Advanced example - downloading in the background
Local $hDownload = InetGet("http://www.autoitscript.com/autoit3/files/beta/update.dat", @TempDir & "\update.dat", 1, 1)
Do
	Sleep(250)
Until InetGetInfo($hDownload, 2) ; Check if the download is complete.
Local $nBytes = InetGetInfo($hDownload, 0)
InetClose($hDownload) ; Close the handle to release resources.
MsgBox(0, "", "Bytes read: " & $nBytes)
