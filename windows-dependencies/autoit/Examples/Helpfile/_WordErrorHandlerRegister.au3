; *******************************************************
; Example 1 - Register and later deregister a custom and the default Word.au3 error handler
; *******************************************************
;
#include <Word.au3>

; Register a customer error handler
_WordErrorHandlerRegister("MyErrFunc")
; Do something
; Deregister the customer error handler
_WordErrorHandlerDeRegister()
; Do something else
; Register the default IE.au3 COM Error Handler
_WordErrorHandlerRegister()
; Do more work

Exit

Func MyErrFunc()
	; Important: the error object variable MUST be named $oWordErrorHandler
	Local $ErrorScriptline = $oWordErrorHandler.scriptline
	Local $ErrorNumber = $oWordErrorHandler.number
	Local $ErrorNumberHex = Hex($oWordErrorHandler.number, 8)
	Local $ErrorDescription = StringStripWS($oWordErrorHandler.description, 2)
	Local $ErrorWinDescription = StringStripWS($oWordErrorHandler.WinDescription, 2)
	Local $ErrorSource = $oWordErrorHandler.Source
	Local $ErrorHelpFile = $oWordErrorHandler.HelpFile
	Local $ErrorHelpContext = $oWordErrorHandler.HelpContext
	Local $ErrorLastDllError = $oWordErrorHandler.LastDllError
	Local $ErrorOutput = ""
	$ErrorOutput &= "--> COM Error Encountered in " & @ScriptName & @CR
	$ErrorOutput &= "----> $ErrorScriptline = " & $ErrorScriptline & @CR
	$ErrorOutput &= "----> $ErrorNumberHex = " & $ErrorNumberHex & @CR
	$ErrorOutput &= "----> $ErrorNumber = " & $ErrorNumber & @CR
	$ErrorOutput &= "----> $ErrorWinDescription = " & $ErrorWinDescription & @CR
	$ErrorOutput &= "----> $ErrorDescription = " & $ErrorDescription & @CR
	$ErrorOutput &= "----> $ErrorSource = " & $ErrorSource & @CR
	$ErrorOutput &= "----> $ErrorHelpFile = " & $ErrorHelpFile & @CR
	$ErrorOutput &= "----> $ErrorHelpContext = " & $ErrorHelpContext & @CR
	$ErrorOutput &= "----> $ErrorLastDllError = " & $ErrorLastDllError
	MsgBox(0, "COM Error", $ErrorOutput)
	SetError(1)
	Return
EndFunc   ;==>MyErrFunc
