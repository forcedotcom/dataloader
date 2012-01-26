;
; AutoIt Version: 3.0
; Language:       English
; Platform:       Win9x/NT
; Author:         Jonathan Bennett (jon@hiddensoft.com)
;
; Script Function:
;   Plays with the calculator.
;


; Prompt the user to run the script - use a Yes/No prompt (4 - see help file)
Local $answer = MsgBox(4, "AutoIt Example (English Only)", "This script will run the calculator and type in 2 x 4 x 8 x 16 and then quit.  Run?")


; Check the user's answer to the prompt (see the help file for MsgBox return values)
; If "No" was clicked (7) then exit the script
If $answer = 7 Then
	MsgBox(0, "AutoIt", "OK.  Bye!")
	Exit
EndIf


; Run the calculator
Run("calc.exe")


; Wait for the calulator become active - it is titled "Calculator" on English systems
WinWaitActive("Calculator")


; Now that the calc window is active type 2 x 4 x 8 x 16
; Use AutoItSetOption to slow down the typing speed so we can see it :)
AutoItSetOption("SendKeyDelay", 400)
Send("2*4*8*16=")
Sleep(2000)


; Now quit by sending a "close" request to the calc
WinClose("Calculator")


; Now wait for calc to close before continuing
WinWaitClose("Calculator")


; Finished!
