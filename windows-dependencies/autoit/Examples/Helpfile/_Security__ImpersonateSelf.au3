#include <SecurityConstants.au3>
#include <Security.au3>
#include <WinAPI.au3>

Local $hToken = _Security__OpenThreadToken($TOKEN_ADJUST_PRIVILEGES)
If $hToken Then
	_WinAPI_CloseHandle($hToken)
Else
	ConsoleWrite("_Security__OpenThreadToken failed with error description: " & _WinAPI_GetLastErrorMessage() & @CRLF)
	ConsoleWrite("New attempt..." & @CRLF)

	; Read remarks for  _Security__OpenThreadToken function
	_Security__ImpersonateSelf()
	$hToken = _Security__OpenThreadToken($TOKEN_ADJUST_PRIVILEGES)
	If $hToken Then
		ConsoleWrite(">>> SUCCESS, $hToken = " & $hToken & @CRLF)
		_WinAPI_CloseHandle($hToken)
	Else
		ConsoleWrite("!FAILED" & @CRLF)
	EndIf
EndIf
