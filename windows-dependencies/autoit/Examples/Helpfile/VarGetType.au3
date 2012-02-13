Local $aArray[2] = [1, "Example"]
Local $bBinary = Binary("0x00204060")
Local $fBoolean = False
Local $hWnd = WinGetHandle("[CLASS:Shell_TrayWnd]")
Local $iInt = 1
Local $nFloat = 2.0
Local $sString = "Some text"
Local $vKeyword = Default

MsgBox(0, "Variable Types", "$aArray is an " & VarGetType($aArray) & " variable type." & @CRLF & _
		"$bBinary is a " & VarGetType($bBinary) & " variable type." & @CRLF & _
		"$fBoolean is a " & VarGetType($fBoolean) & " variable type." & @CRLF & _
		"$hWnd is a " & VarGetType($hWnd) & " variable type." & @CRLF & _
		"$iInt is an " & VarGetType($iInt) & " variable type." & @CRLF & _
		"$nFloat is a " & VarGetType($nFloat) & " variable type." & @CRLF & _
		"$sString is a " & VarGetType($sString) & " variable type." & @CRLF & _
		"$vKeyword is a " & VarGetType($vKeyword) & " variable type." & @CRLF)