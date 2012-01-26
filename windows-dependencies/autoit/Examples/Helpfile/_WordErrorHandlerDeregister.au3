; *******************************************************
; Example 1 - Register and later deregister a custom error handler
; *******************************************************
;
#include <Word.au3>

; Register a customer error handler
_WordErrorHandlerRegister("MyErrFunc")
; Do something
; Deregister the customer error handler
_WordErrorHandlerDeRegister()
; Do something else

Exit

Func MyErrFunc()
	Local $HexNumber = Hex($oWordErrorHandler.number, 8)
	MsgBox(0, "", "We intercepted a COM Error !" & @CRLF & _
			"Number is: " & $HexNumber & @CRLF & _
			"Windescription is: " & $oWordErrorHandler.windescription)
	SetError(1) ; something to check for when this function returns
EndFunc   ;==>MyErrFunc
