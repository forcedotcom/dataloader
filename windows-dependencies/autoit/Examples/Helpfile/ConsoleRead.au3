; Compile this script to "ConsoleRead.exe".
; Open a command prompt to the directory where ConsoleRead.exe resides.
; Type the following on the command line:
;	echo Hello! | ConsoleRead.exe
;
; When invoked in a console window, the above command echos the text "Hello!"
; but instead of dispalying it, the | tells the console to pipe it to the STDIN stream
; of the ConsoleRead.exe process.
If Not @Compiled Then
	MsgBox(0, "", "This script must be compiled in order to properly demonstrate it's functionality.")
	Exit -1
EndIf

Local $data
While True
	$data &= ConsoleRead()
	If @error Then ExitLoop
	Sleep(25)
WEnd
MsgBox(0, "", "Received: " & @CRLF & @CRLF & $data)
