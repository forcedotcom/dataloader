#include <SecurityConstants.au3>
#include <Security.au3>
#include <WinAPI.au3>

Local $hToken = _Security__OpenThreadToken($TOKEN_ADJUST_PRIVILEGES)
If $hToken Then
	ConsoleWrite("$hToken is " & $hToken & @CRLF)
	; $hToken it this thread's token with $TOKEN_ADJUST_PRIVILEGES access

	; ... The rest of the token work here...

	_WinAPI_CloseHandle($hToken)
Else
	ConsoleWrite(_WinAPI_GetLastErrorMessage())
	; Read remarks for _Security__OpenThreadToken
EndIf


