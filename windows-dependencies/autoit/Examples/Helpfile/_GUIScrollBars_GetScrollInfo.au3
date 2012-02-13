#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <StructureConstants.au3>
#include <GuiScrollBars.au3>
#include <ScrollBarConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $GUIMsg, $hGUI, $tSCROLLINFO = DllStructCreate($tagSCROLLINFO)

	$hGUI = GUICreate("ScrollBar Example", 400, 400, -1, -1, BitOR($WS_MINIMIZEBOX, $WS_CAPTION, $WS_POPUP, $WS_SYSMENU, $WS_SIZEBOX))
	$iMemo = GUICtrlCreateEdit("", 2, 2, 380, 360, BitOR($WS_HSCROLL, $WS_VSCROLL))
	GUICtrlSetResizing($iMemo, $GUI_DOCKALL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetBkColor(0x88AABB)

	GUISetState()

	_GUIScrollBars_Init($hGUI)

	DllStructSetData($tSCROLLINFO, "cbSize", DllStructGetSize($tSCROLLINFO))
	DllStructSetData($tSCROLLINFO, "fMask", $_SCROLLBARCONSTANTS_SIF_ALL)
	_GUIScrollBars_GetScrollInfo($hGUI, $SB_HORZ, $tSCROLLINFO)
	MemoWrite("Horizontal" & @CRLF & "--------------------------------------")
	MemoWrite("nPage....: " & DllStructGetData($tSCROLLINFO, "nPage"))
	MemoWrite("nPos.....: " & DllStructGetData($tSCROLLINFO, "nPos"))
	MemoWrite("nMin.....: " & DllStructGetData($tSCROLLINFO, "nMin"))
	MemoWrite("nMax.....: " & DllStructGetData($tSCROLLINFO, "nMax"))
	MemoWrite("nTrackPos: " & DllStructGetData($tSCROLLINFO, "nTrackPos"))

	DllStructSetData($tSCROLLINFO, "cbSize", DllStructGetSize($tSCROLLINFO))
	DllStructSetData($tSCROLLINFO, "fMask", $_SCROLLBARCONSTANTS_SIF_ALL)
	_GUIScrollBars_GetScrollInfo($hGUI, $SB_VERT, $tSCROLLINFO)
	$tSCROLLINFO = _GUIScrollBars_GetScrollInfoEx($hGUI, $SB_VERT)
	MemoWrite(@CRLF & "Vertical" & @CRLF & "--------------------------------------")
	MemoWrite("nPage....: " & DllStructGetData($tSCROLLINFO, "nPage"))
	MemoWrite("nPos.....: " & DllStructGetData($tSCROLLINFO, "nPos"))
	MemoWrite("nMin.....: " & DllStructGetData($tSCROLLINFO, "nMin"))
	MemoWrite("nMax.....: " & DllStructGetData($tSCROLLINFO, "nMax"))
	MemoWrite("nTrackPos: " & DllStructGetData($tSCROLLINFO, "nTrackPos"))

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
