; *******************************************************
; Example 1 - Register and later deregister a custom error handler
; *******************************************************

#include <IE.au3>

; Register a customer error handler
_IEErrorHandlerRegister("MyErrFunc")
; Do something
; Deregister the customer error handler
_IEErrorHandlerDeRegister()
; Do something else

Exit

Func MyErrFunc()
	Local $HexNumber = Hex($oIEErrorHandler.number, 8)
	MsgBox(0, "", "We intercepted a COM Error !" & @CRLF & _
			"Number is: " & $HexNumber & @CRLF & _
			"Windescription is: " & $oIEErrorHandler.windescription)
	SetError(1) ; something to check for when this function returns
EndFunc   ;==>MyErrFunc
