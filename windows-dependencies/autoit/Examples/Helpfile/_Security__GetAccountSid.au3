#include <Security.au3>

Local $tSID = _Security__GetAccountSid(@UserName)
If Not @error Then
	; $tSID is structure filled with SID data for spesified account
	; ... The rest of the script here...
EndIf
