;example1
;get a window handle and use WinGetPos to get the windows' rectangle
Local $hwnd = WinGetHandle("")
Local $coor = WinGetPos($hwnd)

;create the struct
Local $rect = DllStructCreate("int;int;int;int")

;make the DllCall
DllCall("user32.dll", "int", "GetWindowRect", _
		"hwnd", $hwnd, _
		"ptr", DllStructGetPtr($rect)) ; use DllStructGetPtr when calling DllCall

;get the returned rectangle
Local $l = DllStructGetData($rect, 1)
Local $t = DllStructGetData($rect, 2)
Local $r = DllStructGetData($rect, 3)
Local $b = DllStructGetData($rect, 4)

;free the struct
$rect = 0

;display the results of WinGetPos and the returned rectangle
MsgBox(0, "The Larry Test :)", "WinGetPos(): (" & $coor[0] & "," & $coor[1] & _
		") (" & $coor[2] + $coor[0] & "," & $coor[3] + $coor[1] & ")" & @CRLF & _
		"GetWindowRect(): (" & $l & "," & $t & ") (" & $r & "," & $b & ")")

;example2
; DllStructGetPtr referencing an item
Local $a = DllStructCreate("int")
If @error Then
	MsgBox(0, "", "Error in DllStructCreate " & @error);
	Exit
EndIf

$b = DllStructCreate("uint", DllStructGetPtr($a, 1))
If @error Then
	MsgBox(0, "", "Error in DllStructCreate " & @error);
	Exit
EndIf

Local $c = DllStructCreate("float", DllStructGetPtr($a, 1))
If @error Then
	MsgBox(0, "", "Error in DllStructCreate " & @error);
	Exit
EndIf

;set the data
DllStructSetData($a, 1, -1)

;=========================================================
;	Display the different data types of the same data
;=========================================================
MsgBox(0, "DllStruct", _
		"int: " & DllStructGetData($a, 1) & @CRLF & _
		"uint: " & DllStructGetData($b, 1) & @CRLF & _
		"float: " & DllStructGetData($c, 1) & @CRLF & _
		"")

; release memory allocated
$a = 0
