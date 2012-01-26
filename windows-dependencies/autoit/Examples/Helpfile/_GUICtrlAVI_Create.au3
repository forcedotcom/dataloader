#include <GUIConstantsEx.au3>
#include <GuiAVI.au3>
#include <WindowsConstants.au3>

$Debug_AVI = False ; Check ClassName being passed to AVI functions, set to True and use a handle to another control to see it work

Global $hAVI

_Example1()
_Example2()

Func _Example1()
	Local $Wow64 = ""
	If @AutoItX64 Then $Wow64 = "\Wow6432Node"
	Local $hGUI, $sFile = RegRead("HKEY_LOCAL_MACHINE\SOFTWARE" & $Wow64 & "\AutoIt v3\AutoIt", "InstallDir") & "\Examples\GUI\SampleAVI.avi"

	; Create GUI
	$hGUI = GUICreate("(External 1) AVI Create", 300, 100)
	$hAVI = _GUICtrlAVI_Create($hGUI, $sFile, -1, 10, 10)
	GUISetState()

	GUIRegisterMsg($WM_COMMAND, "WM_COMMAND")

	; Play the sample AutoIt AVI
	_GUICtrlAVI_Play($hAVI)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

	; Close AVI clip
	_GUICtrlAVI_Close($hAVI)


	GUIDelete()
EndFunc   ;==>_Example1

Func _Example2()
	Local $hGUI

	; Create GUI
	$hGUI = GUICreate("(External 2) AVI Create", 300, 100)
	$hAVI = _GUICtrlAVI_Create($hGUI, @SystemDir & "\Shell32.dll", 150, 10, 10)
	GUISetState()

	GUIRegisterMsg($WM_COMMAND, "WM_COMMAND")

	; Play the sample AutoIt AVI
	_GUICtrlAVI_Play($hAVI)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

	; Close AVI clip
	_GUICtrlAVI_Close($hAVI)


	GUIDelete()
EndFunc   ;==>_Example2

Func WM_COMMAND($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg
	Local $hWndFrom, $iIDFrom, $iCode
	$hWndFrom = $ilParam
	$iIDFrom = BitAND($iwParam, 0xFFFF) ; Low Word
	$iCode = BitShift($iwParam, 16) ; Hi Word
	Switch $hWndFrom
		Case $hAVI
			Switch $iCode
				Case $ACN_START ; Notifies an animation control's parent window that the associated AVI clip has started playing
					_DebugPrint("$ACN_START" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $ACN_STOP ; Notifies the parent window of an animation control that the associated AVI clip has stopped playing
					_DebugPrint("$ACN_STOP" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
			EndSwitch
	EndSwitch
	Return $GUI_RUNDEFMSG
EndFunc   ;==>WM_COMMAND

Func _DebugPrint($s_text, $line = @ScriptLineNumber)
	ConsoleWrite( _
			"!===========================================================" & @LF & _
			"+======================================================" & @LF & _
			"-->Line(" & StringFormat("%04d", $line) & "):" & @TAB & $s_text & @LF & _
			"+======================================================" & @LF)
EndFunc   ;==>_DebugPrint
