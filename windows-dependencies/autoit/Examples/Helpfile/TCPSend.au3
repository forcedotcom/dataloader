;==============================================
;==============================================
;CLIENT! Start Me after starting the SERVER!!!!!!!!!!!!!!!
;==============================================
;==============================================

Example()

Func Example()
	; Start The TCP Services
	;==============================================
	TCPStartup()

	; Set Some reusable info
	;--------------------------
	Local $ConnectedSocket, $szData
	; Set $szIPADDRESS to wherever the SERVER is. We will change a PC name into an IP Address
	;	Local $szServerPC = @ComputerName
	;	Local $szIPADDRESS = TCPNameToIP($szServerPC)
	Local $szIPADDRESS = @IPAddress1
	Local $nPORT = 33891

	; Initialize a variable to represent a connection
	;==============================================
	$ConnectedSocket = -1

	;Attempt to connect to SERVER at its IP and PORT 33891
	;=======================================================
	$ConnectedSocket = TCPConnect($szIPADDRESS, $nPORT)

	; If there is an error... show it
	If @error Then
		MsgBox(4112, "Error", "TCPConnect failed with WSA error: " & @error)
		; If there is no error loop an inputbox for data
		;   to send to the SERVER.
	Else
		;Loop forever asking for data to send to the SERVER
		While 1
			; InputBox for data to transmit
			$szData = InputBox("Data for Server", @LF & @LF & "Enter data to transmit to the SERVER:")

			; If they cancel the InputBox or leave it blank we exit our forever loop
			If @error Or $szData = "" Then ExitLoop

			; We should have data in $szData... lets attempt to send it through our connected socket.
			; convert AutoIt native UTF-16 to UTF-8
			TCPSend($ConnectedSocket, StringToBinary($szData, 4))

			; If the send failed with @error then the socket has disconnected
			;----------------------------------------------------------------
			If @error Then ExitLoop
		WEnd
	EndIf
EndFunc   ;==>Example
