; *******************************************************
; Example 1 - Register and later deregister a custom and the default IE.au3 error handler
; *******************************************************

#include <IE.au3>

; Register a customer error handler
_IEErrorHandlerRegister("MyErrFunc")
; Do something
; Deregister the customer error handler
_IEErrorHandlerDeRegister()
; Do something else
; Register the default IE.au3 COM Error Handler
_IEErrorHandlerRegister()
; Do more work

Exit

Func MyErrFunc()
	; Important: the error object variable MUST be named $oIEErrorHandler
	Local $ErrorScriptline = $oIEErrorHandler.scriptline
	Local $ErrorNumber = $oIEErrorHandler.number
	Local $ErrorNumberHex = Hex($oIEErrorHandler.number, 8)
	Local $ErrorDescription = StringStripWS($oIEErrorHandler.description, 2)
	Local $ErrorWinDescription = StringStripWS($oIEErrorHandler.WinDescription, 2)
	Local $ErrorSource = $oIEErrorHandler.Source
	Local $ErrorHelpFile = $oIEErrorHandler.HelpFile
	Local $ErrorHelpContext = $oIEErrorHandler.HelpContext
	Local $ErrorLastDllError = $oIEErrorHandler.LastDllError
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
