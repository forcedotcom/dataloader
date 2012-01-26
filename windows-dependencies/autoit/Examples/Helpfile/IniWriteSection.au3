; This is the INI file we will write to.  It will be created on the Desktop.
Local $sIni = @DesktopDir & "\AutoIt-Test.ini"

; Demonstrate creating a new section using a string as input.
Local $sData = "Key1=Value1" & @LF & "Key2=Value2" & @LF & "Key3=Value3"
IniWriteSection($sIni, "Section1", $sData)

; Demonstrate creating a new section using an array as input.
Local $aData1 = IniReadSection($sIni, "Section1") ; Read in what we just wrote above.
For $i = 1 To UBound($aData1) - 1
	$aData1[$i][1] &= "-" & $i ; Change the data some
Next

IniWriteSection($sIni, "Section2", $aData1) ; Write to a new section.

; Demonstrate creating an array manually and using it as input.
Local $aData2[3][2] = [["FirstKey", "FirstValue"],["SecondKey", "SecondValue"],["ThirdKey", "ThirdValue"]]
; Since the array we made starts at element 0, we need to tell IniWriteSection() to start writing from element 0.
IniWriteSection($sIni, "Section3", $aData2, 0)
