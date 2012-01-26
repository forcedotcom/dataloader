#OnAutoItStartRegister "MyTestFunc"
#OnAutoItStartRegister "MyTestFunc2"

Sleep(1000)

Func MyTestFunc()
	MsgBox(64, "Start Results 2", 'Start Message from MyTestFunc()')
EndFunc   ;==>MyTestFunc

Func MyTestFunc2()
	MsgBox(64, "Start Results 3", 'Start Message from MyTestFunc()')
EndFunc   ;==>MyTestFunc2
