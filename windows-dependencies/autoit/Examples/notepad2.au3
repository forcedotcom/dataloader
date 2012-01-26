;
; AutoIt Version: 3.0
; Language:       English
; Platform:       Win9x/NT
; Author:         Jonathan Bennett (jon@hiddensoft.com)
;
; Script Function:
;   Opens Notepad, types in some text and then quits.
;   The text typed shows two ways of Sending special
;   characters
;


; Prompt the user to run the script - use a Yes/No prompt (4 - see help file)
Local $answer = MsgBox(4, "AutoIt Example (English Only)", "This script will run Notepad type in some text and then quit.  Run?")


; Check the user's answer to the prompt (see the help file for MsgBox return values)
; If "No" was clicked (7) then exit the script
If $answer = 7 Then
	MsgBox(0, "AutoIt", "OK.  Bye!")
	Exit
EndIf


; Run Notepad
Run("notepad.exe")


; Wait for the Notepad become active - it is titled "Untitled - Notepad" on English systems
WinWaitActive("[CLASS:Notepad]")


; Now that the Notepad window is active type some special characters
Send("Sending some special characters:{ENTER 2}")

; Do it the first way
Send("First way: ")
Send("{!}{^}{+}{#}")
Send("{ENTER}")

; Do it the second way (RAW mode, notice the second parameter is 1)
Send("Second way: ")
Send("!^+#", 1)

Send("{ENTER}{ENTER}Finished")

; Finished!
