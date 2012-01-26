AdlibRegister("MyAdlib")
;...
AdlibUnRegister("MyAdlib")

Func MyAdlib()
	;... execution must be non blocking, avoid ...Wait(), MsgBox(), InputBox() functions
EndFunc   ;==>MyAdlib
