#include <GuiComboBox.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <Constants.au3>

$Debug_CB = False ; Check ClassName being passed to ComboBox/ComboBoxEx functions, set to True and use a handle to another control to see it work

Global $hCombo

_Main()

Func _Main()
	Local $hGUI

	; Create GUI
	$hGUI = GUICreate("(UDF) ComboBox Create", 400, 296)
	$hCombo = _GUICtrlComboBox_Create($hGUI, "", 2, 2, 396, 296)
	GUISetState()

	; Add files
	_GUICtrlComboBox_BeginUpdate($hCombo)
	_GUICtrlComboBox_AddDir($hCombo, "", $DDL_DRIVES, False)
	_GUICtrlComboBox_EndUpdate($hCombo)

	GUIRegisterMsg($WM_COMMAND, "WM_COMMAND")

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

Func WM_COMMAND($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg
	Local $hWndFrom, $iIDFrom, $iCode
	$hWndFrom = $ilParam
	$iIDFrom = BitAND($iwParam, 0xFFFF) ; Low Word
	$iCode = BitShift($iwParam, 16) ; Hi Word
	Switch $hWndFrom
		Case $hCombo
			Switch $iCode
				Case $CBN_CLOSEUP ; Sent when the list box of a combo box has been closed
					_DebugPrint("$CBN_CLOSEUP" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $CBN_DBLCLK ; Sent when the user double-clicks a string in the list box of a combo box
					_DebugPrint("$CBN_DBLCLK" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $CBN_DROPDOWN ; Sent when the list box of a combo box is about to be made visible
					_DebugPrint("$CBN_DROPDOWN" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $CBN_EDITCHANGE ; Sent after the user has taken an action that may have altered the text in the edit control portion of a combo box
					_DebugPrint("$CBN_EDITCHANGE" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $CBN_EDITUPDATE ; Sent when the edit control portion of a combo box is about to display altered text
					_DebugPrint("$CBN_EDITUPDATE" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $CBN_ERRSPACE ; Sent when a combo box cannot allocate enough memory to meet a specific request
					_DebugPrint("$CBN_ERRSPACE" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $CBN_KILLFOCUS ; Sent when a combo box loses the keyboard focus
					_DebugPrint("$CBN_KILLFOCUS" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $CBN_SELCHANGE ; Sent when the user changes the current selection in the list box of a combo box
					_DebugPrint("$CBN_SELCHANGE" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $CBN_SELENDCANCEL ; Sent when the user selects an item, but then selects another control or closes the dialog box
					_DebugPrint("$CBN_SELENDCANCEL" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $CBN_SELENDOK ; Sent when the user selects a list item, or selects an item and then closes the list
					_DebugPrint("$CBN_SELENDOK" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode)
					; no return value
				Case $CBN_SETFOCUS ; Sent when a combo box receives the keyboard focus
					_DebugPrint("$CBN_SETFOCUS" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
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
