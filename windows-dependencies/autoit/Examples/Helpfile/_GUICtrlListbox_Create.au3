#include <GuiListBox.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <Constants.au3>

$Debug_LB = False ; Check ClassName being passed to ListBox functions, set to True and use a handle to another control to see it work

Global $hListBox

_Main()

Func _Main()
	Local $hGUI

	; Create GUI
	$hGUI = GUICreate("(UDF Created) List Box Create", 400, 296)
	$hListBox = _GUICtrlListBox_Create($hGUI, "String upon creation", 2, 2, 396, 296)
	GUISetState()

	MsgBox(4160, "Information", "Adding Items")

	GUIRegisterMsg($WM_COMMAND, "WM_COMMAND")

	; Add files
	_GUICtrlListBox_BeginUpdate($hListBox)
	_GUICtrlListBox_ResetContent($hListBox)
	_GUICtrlListBox_InitStorage($hListBox, 100, 4096)
	_GUICtrlListBox_Dir($hListBox, @WindowsDir & "\win*.exe")
	_GUICtrlListBox_AddFile($hListBox, @WindowsDir & "\notepad.exe")
	_GUICtrlListBox_Dir($hListBox, "", $DDL_DRIVES)
	_GUICtrlListBox_Dir($hListBox, "", $DDL_DRIVES, False)
	_GUICtrlListBox_EndUpdate($hListBox)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

Func WM_COMMAND($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg
	Local $hWndFrom, $iIDFrom, $iCode, $hWndListBox
	If Not IsHWnd($hListBox) Then $hWndListBox = GUICtrlGetHandle($hListBox)
	$hWndFrom = $ilParam
	$iIDFrom = BitAND($iwParam, 0xFFFF) ; Low Word
	$iCode = BitShift($iwParam, 16) ; Hi Word

	Switch $hWndFrom
		Case $hListBox, $hWndListBox
			Switch $iCode
				Case $LBN_DBLCLK ; Sent when the user double-clicks a string in a list box
					_DebugPrint("$LBN_DBLCLK" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $LBN_ERRSPACE ; Sent when a list box cannot allocate enough memory to meet a specific request
					_DebugPrint("$LBN_ERRSPACE" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $LBN_KILLFOCUS ; Sent when a list box loses the keyboard focus
					_DebugPrint("$LBN_KILLFOCUS" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $LBN_SELCANCEL ; Sent when the user cancels the selection in a list box
					_DebugPrint("$LBN_SELCANCEL" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $LBN_SELCHANGE ; Sent when the selection in a list box has changed
					_DebugPrint("$LBN_SELCHANGE" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $LBN_SETFOCUS ; Sent when a list box receives the keyboard focus
					_DebugPrint("$LBN_SETFOCUS" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
			EndSwitch
	EndSwitch
	; Proceed the default AutoIt3 internal message commands.
	; You also can complete let the line out.
	; !!! But only 'Return' (without any value) will not proceed
	; the default AutoIt3-message in the future !!!
	Return $GUI_RUNDEFMSG
EndFunc   ;==>WM_COMMAND

Func _DebugPrint($s_text)
	$s_text = StringReplace($s_text, @LF, @LF & "-->")
	ConsoleWrite("!===========================================================" & @LF & _
			"+===========================================================" & @LF & _
			"-->" & $s_text & @LF & _
			"+===========================================================" & @LF)
EndFunc   ;==>_DebugPrint
