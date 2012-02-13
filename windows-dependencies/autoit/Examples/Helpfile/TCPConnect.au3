;CLIENT!!!!!!!! Start SERVER First... dummy!!
Local $g_IP = "127.0.0.1"

; Start The TCP Services
;==============================================
TCPStartup()

; Connect to a Listening "SOCKET"
;==============================================
Local $socket = TCPConnect($g_IP, 65432)
If $socket = -1 Then Exit
