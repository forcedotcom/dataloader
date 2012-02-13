Local $sData = InetRead("http://www.autoitscript.com/autoit3/files/beta/update.dat")
Local $nBytesRead = @extended
MsgBox(4096, "", "Bytes read: " & $nBytesRead & @CRLF & @CRLF & BinaryToString($sData))
