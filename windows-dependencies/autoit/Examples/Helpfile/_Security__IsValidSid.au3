#include <Security.au3>

Local $tSID = _Security__GetAccountSid(@ComputerName)
If Not @error Then
	; $tSID is structure filled with SID data for spesified account
	; Check this SID for validity:
	Local $fValid = _Security__IsValidSid($tSID)
	If $fValid Then
		ConsoleWrite("The SID is valid." & @CRLF)
		; ... The rest of the script here...
	Else
		ConsoleWrite("The SID is NOT valid." & @CRLF)
		Exit
	EndIf
EndIf
