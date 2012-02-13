#include <SecurityConstants.au3>
#include <Security.au3>

Local $aArrayOfData = _Security__LookupAccountSid($SID_ALL_SERVICES)

; Print returned data if no error occured
If IsArray($aArrayOfData) Then
	ConsoleWrite("Account name = " & $aArrayOfData[0] & @CRLF)
	ConsoleWrite("Domain name = " & $aArrayOfData[1] & @CRLF)
	ConsoleWrite("SID type = " & _Security__SidTypeStr($aArrayOfData[2]) & @CRLF)
EndIf
