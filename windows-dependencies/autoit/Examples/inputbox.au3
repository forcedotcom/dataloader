;
; AutoIt Version: 3.0
; Language:       English
; Platform:       Win9x/NT
; Author:         Jonathan Bennett (jon@hiddensoft.com)
;
; Script Function:
;   Demonstrates the InputBox, looping and the use of @error.
;


; Prompt the user to run the script - use a Yes/No prompt (4 - see help file)
Local $answer = MsgBox(4, "AutoIt Example (English Only)", "This script will open an input box and get you to type in some text.  Run?")


; Check the user's answer to the prompt (see the help file for MsgBox return values)
; If "No" was clicked (7) then exit the script
If $answer = 7 Then
	MsgBox(4096, "AutoIt", "OK.  Bye!")
	Exit
EndIf

; Loop around until the user gives a valid "autoit" answer
Local $bLoop = 1
While $bLoop = 1
	Local $text = InputBox("AutoIt Example", "Please type in the word ""autoit"" and click OK")
	If @error = 1 Then
		MsgBox(4096, "Error", "You pressed 'Cancel' - try again!")
	Else
		; They clicked OK, but did they type the right thing?
		If $text <> "autoit" Then
			MsgBox(4096, "Error", "You typed in the wrong thing - try again!")
		Else
			$bLoop = 0 ; Exit the loop - ExitLoop would have been an alternative too :)
		EndIf
	EndIf
WEnd

; Print the success message
MsgBox(4096, "AutoIt Example", "You typed in the correct word!  Congrats.")

; Finished!
