Example()

Func Example()
	; Define the string that will be converted later.
	; NOTE: This string may show up as ?? in the help file and even in some editors.
	; This example is saved as UTF-8 with BOM.  It should display correctly in editors
	; which support changing code pages based on BOMs.
	Local Const $sString = "Hello - 你好"

	; Temporary variables used to store conversion results.  $sBinary will hold
	; the original string in binary form and $sConverted will hold the result
	; afte it's been transformed back to the original format.
	Local $sBinary, $sConverted

	; Convert the original UTF-8 string to an ANSI compatible binary string.
	$sBinary = StringToBinary($sString)

	; Convert the ANSI compatible binary string back into a string.
	$sConverted = BinaryToString($sBinary)

	; Display the resulsts.  Note that the last two characters will appear
	; as ?? since they cannot be represented in ANSI.
	DisplayResults($sString, $sBinary, $sConverted, "ANSI")

	; Convert the original UTF-8 string to an UTF16-LE binary string.
	$sBinary = StringToBinary($sString, 2)

	; Convert the UTF16-LE binary string back into a string.
	$sConverted = BinaryToString($sBinary, 2)

	; Display the resulsts.
	DisplayResults($sString, $sBinary, $sConverted, "UTF16-LE")

	; Convert the original UTF-8 string to an UTF16-BE binary string.
	$sBinary = StringToBinary($sString, 3)

	; Convert the UTF16-BE binary string back into a string.
	$sConverted = BinaryToString($sBinary, 3)

	; Display the resulsts.
	DisplayResults($sString, $sBinary, $sConverted, "UTF16-BE")

	; Convert the original UTF-8 string to an UTF-8 binary string.
	$sBinary = StringToBinary($sString, 4)

	; Convert the UTF8 binary string back into a string.
	$sConverted = BinaryToString($sBinary, 4)

	; Display the resulsts.
	DisplayResults($sString, $sBinary, $sConverted, "UTF8")
EndFunc

; Helper function which formats the message for display.  It takes the following parameters:
; $sOriginal - The original string before conversions.
; $sBinary - The original string after it has been converted to binary.
; $sConverted- The string after it has been converted to binary and then back to a string.
; $sConversionType - A human friendly name for the encoding type used for the conversion.
Func DisplayResults($sOriginal, $sBinary, $sConverted, $sConversionType)
	MsgBox(4096, "", "Original:" & @CRLF & $sOriginal & @CRLF & @CRLF & "Binary:" & @CRLF & $sBinary & @CRLF & @CRLF & $sConversionType & ":" & @CRLF & $sConverted)
EndFunc
