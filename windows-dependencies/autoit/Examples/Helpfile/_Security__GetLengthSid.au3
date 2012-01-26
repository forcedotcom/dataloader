#include <Security.au3>

Local $tSID = _Security__GetAccountSid(@UserName)
If Not @error Then
	; $tSID is structure filled with SID data for spesified account
	; Check its length:
	Local $iLength = _Security__GetLengthSid($tSID)
	ConsoleWrite("The length of SID is: " & $iLength & " bytes" & @CRLF)

	; ... The rest of the script here...
EndIf
