Global Enum $E1VAR1, $E1VAR2, $E1VAR3
MsgBox(4096, "", "Expect 0: " & $E1VAR1)
MsgBox(4096, "", "Expect 1: " & $E1VAR2)
MsgBox(4096, "", "Expect 2: " & $E1VAR3)

Global Enum $E2VAR1 = 10, $E2VAR2, $E2VAR3 = 15
MsgBox(4096, "", "Expect 10: " & $E2VAR1)
MsgBox(4096, "", "Expect 11: " & $E2VAR2)
MsgBox(4096, "", "Expect 15: " & $E2VAR3)

Global Enum Step *2 $E3VAR1, $E3VAR2, $E3VAR3
MsgBox(4096, "", "Expect 1: " & $E3VAR1)
MsgBox(4096, "", "Expect 2: " & $E3VAR2)
MsgBox(4096, "", "Expect 4: " & $E3VAR3)
