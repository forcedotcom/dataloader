; *******************************************************
; Example 1 - Obtain current timeout value
; *******************************************************

#include <IE.au3>

Local $iCurrentTimeout = _IELoadWaitTimeout()

; *******************************************************
; Example 2 - Set timeout to 1 minute (60000 milliseconds)
; *******************************************************

#include <IE.au3>

_IELoadWaitTimeout(60000)
