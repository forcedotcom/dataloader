; Word Automation Example
;
; Based on AutoItCOM version 3.1.0
;
; Beta version 06-02-2005

$objWord = ObjCreate("Word.Application")

msgbox (0,"WordTest","Your Word Version is : " & $objWord.Version)
msgbox (0,"WordTest","Build: " & $objWord.Build)

$objWord.Quit		; Get rid of Word


