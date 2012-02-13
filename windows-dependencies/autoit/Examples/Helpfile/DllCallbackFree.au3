
; Create callback function
Local $handle = DllCallbackRegister("_EnumWindowsProc", "int", "hwnd;lparam")

; Call EnumWindows
DllCall("user32.dll", "int", "EnumWindows", "ptr", DllCallbackGetPtr($handle), "lparam", 10)

; Delete callback function
DllCallbackFree($handle)

; Callback Procedure
Func _EnumWindowsProc($hWnd, $lParam)
	If WinGetTitle($hWnd) <> "" And BitAND(WinGetState($hWnd), 2) Then
		Local $res = MsgBox(1, WinGetTitle($hWnd), "$hWnd=" & $hWnd & @CRLF & "lParam=" & $lParam & @CRLF & "$hWnd(type)=" & VarGetType($hWnd))
		If $res = 2 Then Return 0 ; Cancel clicked, return 0 to stop enumeration
	EndIf
	Return 1 ; Return 1 to continue enumeration
EndFunc   ;==>_EnumWindowsProc
