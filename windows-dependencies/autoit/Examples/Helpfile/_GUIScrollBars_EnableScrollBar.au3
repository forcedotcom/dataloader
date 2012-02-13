#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <GuiScrollBars.au3>
#include <ScrollBarConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $GUIMsg, $hGUI

	$hGUI = GUICreate("ScrollBar Example", 400, 400, -1, -1, BitOR($WS_MINIMIZEBOX, $WS_CAPTION, $WS_POPUP, $WS_SYSMENU, $WS_SIZEBOX))
	$iMemo = GUICtrlCreateEdit("", 2, 32, 396, 226, BitOR($WS_HSCROLL, $WS_VSCROLL))
	GUICtrlSetResizing($iMemo, $GUI_DOCKALL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetBkColor(0x88AABB)

	GUISetState()

	_GUIScrollBars_Init($hGUI)

	MemoWrite("Disable down arrow: " & _GUIScrollBars_EnableScrollBar($hGUI, $SB_VERT, $ESB_DISABLE_DOWN))
	Sleep(3000)
	MemoWrite("Disable up arrow: " & _GUIScrollBars_EnableScrollBar($hGUI, $SB_VERT, $ESB_DISABLE_UP))
	Sleep(3000)
	MemoWrite("Enable both arrows: " & _GUIScrollBars_EnableScrollBar($hGUI, $SB_VERT, $ESB_ENABLE_BOTH))
	Sleep(3000)
	MemoWrite("Disable left arrow: " & _GUIScrollBars_EnableScrollBar($hGUI, $SB_HORZ, $ESB_DISABLE_LEFT))
	Sleep(3000)
	MemoWrite("Disable right arrow: " & _GUIScrollBars_EnableScrollBar($hGUI, $SB_HORZ, $ESB_DISABLE_RIGHT))
	Sleep(3000)
	MemoWrite("Enable both arrows: " & _GUIScrollBars_EnableScrollBar($hGUI, $SB_HORZ, $ESB_ENABLE_BOTH))

	While 1
		$GUIMsg = GUIGetMsg()

		Switch $GUIMsg
			Case $GUI_EVENT_CLOSE;, $nExititem
				ExitLoop
		EndSwitch
	WEnd

	Exit
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
