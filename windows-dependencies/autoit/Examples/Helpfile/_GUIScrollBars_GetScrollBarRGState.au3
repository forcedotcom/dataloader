#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <GuiScrollBars.au3>
#include <ScrollBarConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $GUIMsg, $hGUI, $aRGState

	$hGUI = GUICreate("ScrollBar Example", 400, 400, -1, -1, BitOR($WS_MINIMIZEBOX, $WS_CAPTION, $WS_POPUP, $WS_SYSMENU, $WS_SIZEBOX))
	$iMemo = GUICtrlCreateEdit("", 2, 2, 380, 360, BitOR($WS_HSCROLL, $WS_VSCROLL))
	GUICtrlSetResizing($iMemo, $GUI_DOCKALL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetBkColor(0x88AABB)

	GUISetState()

	_GUIScrollBars_Init($hGUI)

	$aRGState = _GUIScrollBars_GetScrollBarRGState($hGUI, $OBJID_HSCROLL)
	MemoWrite("Horizontal (Before)" & @CRLF & "--------------------------------------")
	For $x = 0 To 5
		MemoWrite("rgstate[" & $x & "]...: " & $aRGState[$x])
	Next

	MemoWrite(@CRLF & "Disable both arrows: " & _GUIScrollBars_EnableScrollBar($hGUI, $SB_HORZ, $ESB_DISABLE_BOTH) & @CRLF)

	$aRGState = _GUIScrollBars_GetScrollBarRGState($hGUI, $OBJID_HSCROLL)
	MemoWrite("Horizontal (After)" & @CRLF & "--------------------------------------")
	For $x = 0 To 5
		MemoWrite("rgstate[" & $x & "]...: " & $aRGState[$x])
	Next

	While 1
		$GUIMsg = GUIGetMsg()

		Switch $GUIMsg
			Case $GUI_EVENT_CLOSE
				ExitLoop
		EndSwitch
	WEnd

	Exit
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
