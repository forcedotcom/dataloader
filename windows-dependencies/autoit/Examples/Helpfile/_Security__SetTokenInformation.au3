#RequireAdmin ; for this example to have sense

#include <SecurityConstants.au3>
#include <Security.au3>
#include <WinAPI.au3>

Example_SeTokInfo()

Func Example_SeTokInfo()
	Local $hProcess = _WinAPI_GetCurrentProcess()
	If @error Then Return ; check for possible errors

	Local $hToken = _Security__OpenProcessToken($hProcess, $TOKEN_ALL_ACCESS)

	; If token is get...
	If $hToken Then
		; Set Medium Integrity Level for this token.
		Local $tSID = _Security__StringSidToSid($SID_MEDIUM_MANDATORY_LEVEL)
		; Define TOKEN_MANDATORY_LABEL structure
		Local Const $tTOKEN_MANDATORY_LABEL = DllStructCreate("ptr Sid; dword Attributes")
		; Fill it with wanted data
		DllStructSetData($tTOKEN_MANDATORY_LABEL, "Sid", DllStructGetPtr($tSID, 1))
		DllStructSetData($tTOKEN_MANDATORY_LABEL, "Attributes", $SE_GROUP_INTEGRITY)

		If _Security__SetTokenInformation($hToken, $TOKENINTEGRITYLEVEL, $tTOKEN_MANDATORY_LABEL, DllStructGetSize($tTOKEN_MANDATORY_LABEL)) Then

			; Default IL is $SID_HIGH_MANDATORY_LEVEL, however...
			MsgBox(262144, "SetTokenInformation", "$hToken = " & $hToken & @CRLF & "This token have non-default Medium Integrity Level")

			; ... Do something with token here ...

		EndIf
		; Close the token handle when no longer needed
		_WinAPI_CloseHandle($hToken)
	EndIf
EndFunc   ;==>Example_SeTokInfo

