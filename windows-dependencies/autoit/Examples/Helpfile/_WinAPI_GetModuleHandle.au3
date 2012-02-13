#include <WinAPI.au3>
#include <WindowsConstants.au3>
#include <StructureConstants.au3>

Global $hHook, $hStub_KeyProc, $buffer = ""

_Main()

Func _Main()
	OnAutoItExitRegister("Cleanup")

	Local $hmod

	$hStub_KeyProc = DllCallbackRegister("_KeyProc", "long", "int;wparam;lparam")
	$hmod = _WinAPI_GetModuleHandle(0)
	$hHook = _WinAPI_SetWindowsHookEx($WH_KEYBOARD_LL, DllCallbackGetPtr($hStub_KeyProc), $hmod)

	MsgBox(4096, "", "Click OK, then in notepad type..." & _
			@LF & @LF & "Jon" & @LF & "AutoIt" & @LF & @LF & "Press Esc to exit script")

	Run("notepad.exe")
	WinWait("[CLASS:Notepad]")
	WinActivate("[CLASS:Notepad]")

	While 1
		Sleep(10)
	WEnd
EndFunc   ;==>_Main

Func EvaluateKey($keycode)
	If (($keycode > 64) And ($keycode < 91)) _ ; a - z
			Or (($keycode > 96) And ($keycode < 123)) _ ; A - Z
			Or (($keycode > 47) And ($keycode < 58)) Then ; 0 - 9
		$buffer &= Chr($keycode)
		Switch $buffer
			Case "Jon"
				ToolTip("What can you say?")
			Case "AutoIt"
				ToolTip("AutoIt Rocks")
		EndSwitch
	ElseIf ($keycode > 159) And ($keycode < 164) Then
		Return
	ElseIf ($keycode = 27) Then ; esc key
		Exit
	Else
		$buffer = ""
	EndIf
EndFunc   ;==>EvaluateKey

;===========================================================
; callback function
;===========================================================
Func _KeyProc($nCode, $wParam, $lParam)
	Local $tKEYHOOKS
	$tKEYHOOKS = DllStructCreate($tagKBDLLHOOKSTRUCT, $lParam)
	If $nCode < 0 Then
		Return _WinAPI_CallNextHookEx($hHook, $nCode, $wParam, $lParam)
	EndIf
	If $wParam = $WM_KEYDOWN Then
		EvaluateKey(DllStructGetData($tKEYHOOKS, "vkCode"))
	Else
		Local $flags = DllStructGetData($tKEYHOOKS, "flags")
		Switch $flags
			Case $LLKHF_ALTDOWN
				ConsoleWrite("$LLKHF_ALTDOWN" & @CRLF)
			Case $LLKHF_EXTENDED
				ConsoleWrite("$LLKHF_EXTENDED" & @CRLF)
			Case $LLKHF_INJECTED
				ConsoleWrite("$LLKHF_INJECTED" & @CRLF)
			Case $LLKHF_UP
				ConsoleWrite("$LLKHF_UP: scanCode - " & DllStructGetData($tKEYHOOKS, "scanCode") & @TAB & "vkCode - " & DllStructGetData($tKEYHOOKS, "vkCode") & @CRLF)
		EndSwitch
	EndIf
	Return _WinAPI_CallNextHookEx($hHook, $nCode, $wParam, $lParam)
EndFunc   ;==>_KeyProc

Func Cleanup()
	_WinAPI_UnhookWindowsHookEx($hHook)
	DllCallbackFree($hStub_KeyProc)
EndFunc   ;==>Cleanup
