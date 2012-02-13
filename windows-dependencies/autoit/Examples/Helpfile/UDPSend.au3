;;This is the UDP Client
;;Start the server first

; Start The UDP Services
;==============================================
UDPStartup()

; Register the cleanup function.
OnAutoItExitRegister("Cleanup")

; Open a "SOCKET"
;==============================================
Local $socket = UDPOpen("127.0.0.1", 65532)
If @error <> 0 Then Exit

Local $n = 0
While 1
	Sleep(2000)
	$n = $n + 1
	Local $status = UDPSend($socket, "Message #" & $n)
	If $status = 0 Then
		MsgBox(0, "ERROR", "Error while sending UDP message: " & @error)
		Exit
	EndIf
WEnd

Func Cleanup()
	UDPCloseSocket($socket)
	UDPShutdown()
EndFunc   ;==>Cleanup
