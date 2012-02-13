;
; AutoIt Version: 3.0
; Language:       English
; Platform:       Win9x/NT
; Author:         Jonathan Bennett (jon@hiddensoft.com)
;
; Script Function:
;   Demo of using functions
;


; Prompt the user to run the script - use a Yes/No prompt (4 - see help file)
Local $answer = MsgBox(4, "AutoIt Example", "This script will call a couple of example functions.  Run?")


; Check the user's answer to the prompt (see the help file for MsgBox return values)
; If "No" was clicked (7) then exit the script
If $answer = 7 Then
	MsgBox(0, "AutoIt", "OK.  Bye!")
	Exit
EndIf


; Run TestFunc1
TestFunc1()

; Run TestFunc2
TestFunc2(20)

; Finished!
MsgBox(0, "AutoIt Example", "Finished!")
Exit


; TestFunc1
Func TestFunc1()
	MsgBox(0, "AutoIt Example", "Inside TestFunc1()")
EndFunc   ;==>TestFunc1


; TestFunc2
Func TestFunc2($var)
	MsgBox(0, "AutoIt Example", "Inside TestFunc2() - $var is: " & $var)
EndFunc   ;==>TestFunc2
