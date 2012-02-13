WinMove("[active]", "", Default, Default, 200, 300) ; just resize the active window (no move)

MyFunc2(Default, Default)

Func MyFunc2($Param1 = Default, $Param2 = 'Two', $Param3 = Default)
	If $Param1 = Default Then $Param1 = 'One'
	If $Param3 = Default Then $Param3 = 'Three'

	MsgBox(0, 'Params', '1 = ' & $Param1 & @LF & _
			'2 = ' & $Param2 & @LF & _
			'3 = ' & $Param3)
EndFunc   ;==>MyFunc2
