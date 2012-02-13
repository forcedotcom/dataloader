#RequireAdmin ; for this example to have sense

#include <SecurityConstants.au3>
#include <Security.au3>
#include <WinAPI.au3>

Local $hToken = _Security__OpenProcessToken(_WinAPI_GetCurrentProcess(), $TOKEN_ALL_ACCESS)
If $hToken Then
	; $hToken is this process' token with $TOKEN_ALL_ACCESS access

	; Disable all privileges for this token
	If _Security__AdjustTokenPrivileges($hToken, True, 0, 0) Then
		;... Do whatever with this token now and here...
		MsgBox(262144, "TokenPrivileges", "All TokenPrivileges disabled!")
	EndIf

	; Close handle when done
	_WinAPI_CloseHandle($hToken)
EndIf
