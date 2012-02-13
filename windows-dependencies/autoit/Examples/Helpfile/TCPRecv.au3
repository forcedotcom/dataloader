#include <GUIConstantsEx.au3>

;==============================================
;==============================================
;SERVER!! Start Me First !!!!!!!!!!!!!!!
;==============================================
;==============================================

Example()

Func Example()
	; Set Some reusable info
	; Set your Public IP address (@IPAddress1) here.
	;	Local $szServerPC = @ComputerName
	;	Local $szIPADDRESS = TCPNameToIP($szServerPC)
	Local $szIPADDRESS = @IPAddress1
	Local $nPORT = 33891
	Local $MainSocket, $edit, $ConnectedSocket, $szIP_Accepted
	Local $msg, $recv

	; Start The TCP Services
	;==============================================
	TCPStartup()

	; Create a Listening "SOCKET".
	;   Using your IP Address and Port 33891.
	;==============================================
	$MainSocket = TCPListen($szIPADDRESS, $nPORT)

	; If the Socket creation fails, exit.
	If $MainSocket = -1 Then Exit


	; Create a GUI for messages
	;==============================================
	GUICreate("My Server (IP: " & $szIPADDRESS & ")", 300, 200, 100, 100)
	$edit = GUICtrlCreateEdit("", 10, 10, 280, 180)
	GUISetState()


	; Initialize a variable to represent a connection
	;==============================================
	$ConnectedSocket = -1


	;Wait for and Accept a connection
	;==============================================
	Do
		$ConnectedSocket = TCPAccept($MainSocket)
	Until $ConnectedSocket <> -1


	; Get IP of client connecting
	$szIP_Accepted = SocketToIP($ConnectedSocket)

	; GUI Message Loop
	;==============================================
	While 1
		$msg = GUIGetMsg()

		; GUI Closed
		;--------------------
		If $msg = $GUI_EVENT_CLOSE Then ExitLoop

		; Try to receive (up to) 2048 bytes
		;----------------------------------------------------------------
		$recv = TCPRecv($ConnectedSocket, 2048)

		; If the receive failed with @error then the socket has disconnected
		;----------------------------------------------------------------
		If @error Then ExitLoop

		; convert from UTF-8 to AutoIt native UTF-16
		$recv = BinaryToString($recv, 4)

		; Update the edit control with what we have received
		;----------------------------------------------------------------
		If $recv <> "" Then GUICtrlSetData($edit, _
				$szIP_Accepted & " > " & $recv & @CRLF & GUICtrlRead($edit))
	WEnd


	If $ConnectedSocket <> -1 Then TCPCloseSocket($ConnectedSocket)

	TCPShutdown()
EndFunc   ;==>Example

; Function to return IP Address from a connected socket.
;----------------------------------------------------------------------
Func SocketToIP($SHOCKET)
	Local $sockaddr, $aRet

	$sockaddr = DllStructCreate("short;ushort;uint;char[8]")

	$aRet = DllCall("Ws2_32.dll", "int", "getpeername", "int", $SHOCKET, _
			"ptr", DllStructGetPtr($sockaddr), "int*", DllStructGetSize($sockaddr))
	If Not @error And $aRet[0] = 0 Then
		$aRet = DllCall("Ws2_32.dll", "str", "inet_ntoa", "int", DllStructGetData($sockaddr, 3))
		If Not @error Then $aRet = $aRet[0]
	Else
		$aRet = 0
	EndIf

	$sockaddr = 0

	Return $aRet
EndFunc   ;==>SocketToIP
