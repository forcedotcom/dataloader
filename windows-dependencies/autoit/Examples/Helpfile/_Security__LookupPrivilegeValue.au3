#include <SecurityConstants.au3>
#include <Security.au3>

Local $vLUID = _Security__LookupPrivilegeValue("", $SE_DEBUG_NAME)
; $vLUID represents LUID for a privilege value in form of 64bit integer. Print it out of curiosity:
ConsoleWrite("$vLUID = " & $vLUID & @CRLF)
; ... The rest of the script here...