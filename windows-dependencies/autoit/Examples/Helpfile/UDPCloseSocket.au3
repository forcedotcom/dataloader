;SERVER!! Start Me First !!!!!!!!!!!!!!!
Local $g_IP = "127.0.0.1"

; Start The UDP Services
;==============================================
UDPStartup()

; Register the cleanup function.
OnAutoItExitRegister("Cleanup")

; Create a Listening "SOCKET"
;==============================================
Local $socket = UDPBind($g_IP, 65432)
If @error <> 0 Then Exit

;--- Your code here

Func Cleanup()
	UDPCloseSocket($socket)
	UDPShutdown()
EndFunc   ;==>Cleanup
