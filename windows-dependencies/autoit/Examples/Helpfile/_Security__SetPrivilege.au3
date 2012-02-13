#RequireAdmin ; for this example to have sense

#include <SecurityConstants.au3>
#include <Security.au3>
#include <WinAPI.au3>
#include <Date.au3>

Local $hToken = _Security__OpenProcessToken(_WinAPI_GetCurrentProcess(), $TOKEN_ALL_ACCESS)
If $hToken Then
	; $hToken it this process' token with $TOKEN_ALL_ACCESS access

	; Enable SeDebugPrivilege for this token
	If _Security__SetPrivilege($hToken, $SE_DEBUG_NAME, True) Then
		;... Do whatever with this token now and here...
		MsgBox(262144, "TokenPrivileges", $SE_DEBUG_NAME & " enabled!")
		; Disable
		_Security__SetPrivilege($hToken, $SE_DEBUG_NAME, False)
		MsgBox(262144, "TokenPrivileges", $SE_DEBUG_NAME & " disabled!")
	EndIf

	; Close handle when done
	_WinAPI_CloseHandle($hToken)
EndIf
