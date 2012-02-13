#RequireAdmin ; for this example to have sense

#include <ProcessConstants.au3>
#include <StructureConstants.au3>
#include <SecurityConstants.au3>
#include <Security.au3>
#include <WinAPI.au3>

Example_ProcessWithTok()


Func Example_ProcessWithTok()
	; Run AutoIt non-elevated regardless of having full administrator rights obtained using #RequireAdmin or by any other means
	_RunNonElevated('"' & @AutoItExe & '" /AutoIt3ExecuteLine  "MsgBox(262144, ''RunNonElevated'', ''IsAdmin() = '' & "IsAdmin()" & '', PID = '' & "@AutoItPID")"')
EndFunc   ;==>Example_ProcessWithTok


Func _RunNonElevated($sCommandLine = "")
	If Not IsAdmin() Then Return Run($sCommandLine) ; if current process is run non-elevated then just Run new one.

	; Structures needed for creating process
	Local $tSTARTUPINFO = DllStructCreate($tagSTARTUPINFO)
	Local $tPROCESS_INFORMATION = DllStructCreate($tagPROCESS_INFORMATION)

	; Process handle of some process that's run non-elevated. For example "Explorer"
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
			; If successful
			If $hTokDuplicate Then
				; Create process with this new token
				_Security__CreateProcessWithToken($hTokDuplicate, 0, $sCommandLine, 0, @ScriptDir, $tSTARTUPINFO, $tPROCESS_INFORMATION)

				; Close that token
				_WinAPI_CloseHandle($hTokDuplicate)
				; Close get handles
				_WinAPI_CloseHandle(DllStructGetData($tPROCESS_INFORMATION, "hProcess"))
				_WinAPI_CloseHandle(DllStructGetData($tPROCESS_INFORMATION, "hThread"))
				; Return PID of newly created process
				Return DllStructGetData($tPROCESS_INFORMATION, "ProcessID")
			EndIf
		EndIf
	EndIf
EndFunc   ;==>_RunNonElevated
