Test1()
Test2()
Test3()

; This example demonstrates a basic replacement.  It replaces the vowels aeiou
; with the @ character.
Func Test1()
	Local $sInput = "Where have all the flowers gone, long time passing?"
	Local $sOutput = StringRegExpReplace($sInput, "[aeiou]", "@")
	Display($sInput, $sOutput)
EndFunc   ;==>Test1

; The following example demonstrates using back-references to change the date
; from MM/DD/YYYY to DD.MM.YYYY
Func Test2()
	Local $sInput = 'some text1 12/31/2009 01:02:03 some text2' & @CRLF & _
			'some text3 02/28/2009 11:22:33 some text4'
	Local $sOutput = StringRegExpReplace($sInput, '(\d{2})/(\d{2})/(\d{4})', ' $2.$1.$3 ')
	Display($sInput, $sOutput)
EndFunc   ;==>Test2

; The following example demonstrates the need to double backslash
Func Test3()
	Local $sInput = '%CommonProgramFiles%\Microsoft Shared\'
	Local $sOutput = StringRegExpReplace($sInput, '%([^%]*?)%', 'C:\\WINDOWS\\Some Other Folder$')
	Display($sInput, $sOutput)
EndFunc   ;==>Test3

Func Display($sInput, $sOutput)
	; Format the output.
	Local $sMsg = StringFormat("Input:\t%s\n\nOutput:\t%s", $sInput, $sOutput)
	MsgBox(0, "Results", $sMsg)
EndFunc   ;==>Display
