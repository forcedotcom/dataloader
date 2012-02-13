; This calls a function accepting no arguments.
Call("Test1")

; This calls a function accepting one argument and passes it an argument.
Call("Test2", "Message from Call()!")

; This demonstrates how to use the special array argument.
Global $aArgs[4]
$aArgs[0] = "CallArgArray" ; This is required, otherwise, Call() will not recognize the array as containing arguments
$aArgs[1] = "This is a string" ; Parameter one is a string
$aArgs[2] = 47 ; Parameter two is a number
Global $array[2]
$array[0] = "Array Element 0"
$array[1] = "Array Element 1"
$aArgs[3] = $array ; Parameter three is an array

; We've built the special array, now call the function
Call("Test3", $aArgs)

; Test calling a function that does not exist.  This shows the proper way to test by
; checking that both @error and @extended contain the documented failure values.
Local Const $sFunction = "DoesNotExist"
Call($sFunction)
If @error = 0xDEAD And @extended = 0xBEEF Then MsgBox(4096, "", "Function does not exist.")

Func Test1()
	MsgBox(4096, "", "Hello")
EndFunc   ;==>Test1

Func Test2($sMsg)
	MsgBox(4096, "", $sMsg)
EndFunc   ;==>Test2

Func Test3($sString, $nNumber, $aArray)
	MsgBox(4096, "", "The string is: " & @CRLF & $sString)
	MsgBox(4096, "", "The number is: " & @CRLF & $nNumber)
	For $i = 0 To UBound($aArray) - 1
		MsgBox(4096, "", "Array[" & $i & "] contains:" & @CRLF & $aArray[$i])
	Next
EndFunc   ;==>Test3
