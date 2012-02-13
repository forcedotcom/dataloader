;SERVER!! Start Me First !!!!!!!!!!!!!!!
Local $g_IP = "127.0.0.1"

; Start The UDP Services
;==============================================
UDPStartup()

; Create a Listening "SOCKET"
;==============================================
Local $socket = UDPBind($g_IP, 65432)
If @error <> 0 Then Exit

