;SERVER!! Start Me First !!!!!!!!!!!!!!!
Local $g_IP = "127.0.0.1"

; Start The TCP Services
;==============================================
TCPStartup()

; Create a Listening "SOCKET"
;==============================================
Local $MainSocket = TCPListen($g_IP, 65432, 100)
If $MainSocket = -1 Then Exit

;  look for client connection
;--------------------
While 1
	Local $ConnectedSocket = TCPAccept($MainSocket)
	If $ConnectedSocket >= 0 Then
		MsgBox(0, "", "my server - Client Connected")
		Exit
	EndIf
WEnd
