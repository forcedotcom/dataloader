#include <ProcessConstants.au3>
#include <SecurityConstants.au3>
#include <Security.au3>
#include <WinAPI.au3>

Local $hProcess = _WinAPI_OpenProcess($PROCESS_ALL_ACCESS, 0, ProcessExists("explorer.exe"))
; If successful
If $hProcess Then
	; Token...
	Local $hTokOriginal = _Security__OpenProcessToken($hProcess, $TOKEN_ALL_ACCESS)
	; Process handle is no longer needed. Close it
	_WinAPI_CloseHandle($hProcess)
	; If successful
	If $hTokOriginal Then
		; Duplicate the original token
		Local $hTokDuplicate = _Security__DuplicateTokenEx($hTokOriginal, $TOKEN_ALL_ACCESS, $SECURITYIMPERSONATION, $TOKENPRIMARY)
		; Close the original token
		_WinAPI_CloseHandle($hTokOriginal)
		; What's created is a primary token (!)
		; ... Do whatever with that token here ...

		MsgBox(262144, "DuplicateTokenEx", "$hTokDuplicate = " & $hTokDuplicate)

		; Close that token when done
		_WinAPI_CloseHandle($hTokDuplicate)
	EndIf
EndIf

