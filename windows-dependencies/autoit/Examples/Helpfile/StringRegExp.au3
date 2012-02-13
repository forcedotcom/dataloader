;Option 1, using offset
Local $nOffset = 1

Local $array
While 1
	$array = StringRegExp('<test>a</test> <test>b</test> <test>c</Test>', '<(?i)test>(.*?)</(?i)test>', 1, $nOffset)

	If @error = 0 Then
		$nOffset = @extended
	Else
		ExitLoop
	EndIf
	For $i = 0 To UBound($array) - 1
		MsgBox(0, "RegExp Test with Option 1 - " & $i, $array[$i])
	Next
WEnd


;Option 2, single return, php/preg_match() style
$array = StringRegExp('<test>a</test> <test>b</test> <test>c</Test>', '<(?i)test>(.*?)</(?i)test>', 2)
For $i = 0 To UBound($array) - 1
	MsgBox(0, "RegExp Test with Option 2 - " & $i, $array[$i])
Next


;Option 3, global return, old AutoIt style
$array = StringRegExp('<test>a</test> <test>b</test> <test>c</Test>', '<(?i)test>(.*?)</(?i)test>', 3)

For $i = 0 To UBound($array) - 1
	MsgBox(0, "RegExp Test with Option 3 - " & $i, $array[$i])
Next


;Option 4, global return, php/preg_match_all() style
$array = StringRegExp('F1oF2oF3o', '(F.o)*?', 4)

For $i = 0 To UBound($array) - 1

	Local $match = $array[$i]
	For $j = 0 To UBound($match) - 1
		MsgBox(0, "cRegExp Test with Option 4 - " & $i & ',' & $j, $match[$j])
	Next
Next
