#include <Date.au3>
#include <WinAPI.au3>

; Under Vista the Windows API "SetSystemTimeAdjustment" may be rejected due to system security

_Main()

Func _Main()
	Local $aInfo

	; Open the clock so we can watch the fun
	Run("RunDll32.exe shell32.dll,Control_RunDLL timedate.cpl")
	WinWaitActive("[CLASS:#32770]")

	; Get current time adjustments
	$aInfo = _Date_Time_GetSystemTimeAdjustment()

	; Slow down clock
	If Not _Date_Time_SetSystemTimeAdjustment($aInfo[1] / 10, False) Then
		MsgBox(4096, "Error", "System clock cannot be DOWN" & @CRLF & @CRLF & _WinAPI_GetLastErrorMessage())
		Exit
	EndIf
	MsgBox(4096, "Information", "Slowing down system clock", 2)

	Sleep(5000)

	; Speed up clock
	If Not _Date_Time_SetSystemTimeAdjustment($aInfo[1] * 10, False) Then
		MsgBox(4096, "Error", "System clock cannot be UP" & @CRLF & @CRLF & _WinAPI_GetLastErrorMessage())
	EndIf
	MsgBox(4096, "Information", "Speeding up system clock", 2)

	Sleep(5000)

	; Reset time adjustment
	If Not _Date_Time_SetSystemTimeAdjustment($aInfo[1], True) Then
		MsgBox(4096, "Error", "System clock cannot be RESET" & @CRLF & @CRLF & _WinAPI_GetLastErrorMessage())
	Else
		MsgBox(4096, "Information", "System clock restored")
	EndIf

EndFunc   ;==>_Main
