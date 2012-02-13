OnAutoItExitRegister("MyTestFunc")
OnAutoItExitRegister("MyTestFunc2")

Sleep(1000)

Func MyTestFunc()
	MsgBox(64, "Exit Results 1", 'Exit Message from MyTestFunc()')
EndFunc   ;==>MyTestFunc

Func MyTestFunc2()
	MsgBox(64, "Exit Results 2", 'Exit Message from MyTestFunc()')
EndFunc   ;==>MyTestFunc2
