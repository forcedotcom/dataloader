Local $var = 0
Local $var2 = ""

Select
	Case $var = 1
		MsgBox(0, "", "First Case expression was true")
	Case $var2 = "test"
		MsgBox(0, "", "Second Case expression was true")
	Case Else
		MsgBox(0, "", "No preceding case was true!")
EndSelect
