;CLIENT!!!!!!!! Start SERVER First... dummy!!
Local $g_IP = "127.0.0.1"

; Start The UDP Services
;==============================================
UDPStartup()

; Connect to a Listening "SOCKET"
;==============================================
Local $socket = UDPOpen($g_IP, 65432)
If @error <> 0 Then Exit
