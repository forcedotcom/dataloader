;SERVER!! Start Me First !!!!!!!!!!!!!!!
Local $g_IP = "127.0.0.1"

; Start The TCP Services
;==============================================
TCPStartup()

; Create a Listening "SOCKET"
;==============================================
Local $MainSocket = TCPListen($g_IP, 65432, 100)
If $MainSocket = -1 Then Exit
