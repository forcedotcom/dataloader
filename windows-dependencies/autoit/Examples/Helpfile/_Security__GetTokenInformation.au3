#include <SecurityConstants.au3>
#include <Security.au3>
#include <WinAPI.au3>

Example_TokInfo()

Func Example_TokInfo()
	Local $hProcess = _WinAPI_GetCurrentProcess()
	If @error Then Return ; check for possible errors

	Local $hToken = _Security__OpenProcessToken($hProcess, $TOKEN_ALL_ACCESS)
	; If token is get...
	If $hToken Then
		; Get information about the type of this token:
		Local $tInfo = _Security__GetTokenInformation($hToken, $TOKENTYPE)
		; The result will be raw binary data. For $TOKENTYPE it's TOKEN_TYPE value (enum value). Reinterpreting as "int" type therefore:
		Local $iTokenType = DllStructGetData(DllStructCreate("int", DllStructGetPtr($tInfo)), 1)

		ConsoleWrite("Token type is " & $iTokenType & @CRLF) ; Can be value of either $TOKENPRIMARY or $TOKENIMPERSONATION

		; Close the token handle
		_WinAPI_CloseHandle($hToken)
	EndIf
EndFunc   ;==>Example_TokInfo
