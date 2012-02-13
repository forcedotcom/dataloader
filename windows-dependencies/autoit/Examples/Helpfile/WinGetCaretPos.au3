Local $a = WinGetCaretPos()
If Not @error Then ToolTip("First Method Pos", $a[0], $a[1])
Sleep(2000)

Local $b = _CaretPos()
If Not @error Then ToolTip("Second Method Pos", $b[0], $b[1])
Sleep(2000)

; More reliable method to get caret coords in MDI text editors.
Func _CaretPos()
	Local $x_adjust = 5
	Local $y_adjust = 40

	Opt("CaretCoordMode", 0) ;relative mode
	Local $c = WinGetCaretPos() ;relative caret coords
	Local $w = WinGetPos("") ;window's coords
	Local $f = ControlGetFocus("", "") ;text region "handle"
	Local $e = ControlGetPos("", "", $f) ;text region coords

	Local $t[2]
	If IsArray($c) And IsArray($w) And IsArray($e) Then
		$t[0] = $c[0] + $w[0] + $e[0] + $x_adjust
		$t[1] = $c[1] + $w[1] + $e[1] + $y_adjust
		Return $t ;absolute screen coords of caret cursor
	Else
		SetError(1)
	EndIf
EndFunc   ;==>_CaretPos
