;
; AutoIt Version: 3.0
; Language:       English
; Platform:       Win9x/NT
; Author:         Jonathan Bennett (jon at hiddensoft com)
;
; Script Function:
;   Counts to 5 using a "while" loop


; Prompt the user to run the script - use a Yes/No prompt (4 - see help file)
Local $answer = MsgBox(4, "AutoIt Example", "This script will count to 5 using a 'While' loop.  Run?")


; Check the user's answer to the prompt (see the help file for MsgBox return values)
; If "No" was clicked (7) then exit the script
If $answer = 7 Then
	MsgBox(0, "AutoIt Example", "OK.  Bye!")
	Exit
EndIf


; Set the counter
Local $count = 0

; Execute the loop "While" the counter is less than 5
While $count < 5
	; Increase the count by one
	$count = $count + 1

	; Print the count
	MsgBox(0, "AutoIt Example", "Count is: " & $count)
WEnd

; Finished!
MsgBox(0, "AutoIt Example", "Finished!")
