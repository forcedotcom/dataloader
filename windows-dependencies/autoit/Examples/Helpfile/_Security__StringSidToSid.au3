#include <SecurityConstants.au3>
#include <Security.au3>

Local $tSID = _Security__StringSidToSid($SID_ADMINISTRATORS)
If Not @error Then
	; $tSID is structure filled with SID data for spesified string form SID
	; ... The rest of the script here...
EndIf
