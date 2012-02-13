
;;; SCRIPT.AU3 ;;;
#include "include-library.au3"  ;throws an error if #include-once was not used

MsgBox(0, "Example", "This is from 'script.au3' file")
myFunc()

; Running script.au3 will output the two message boxes:
; one saying "This is from 'script.au3' file"
; and another saying "Hello from library.au3"
