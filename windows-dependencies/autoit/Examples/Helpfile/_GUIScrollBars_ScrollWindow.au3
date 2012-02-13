#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <StructureConstants.au3>
#include <GuiScrollBars.au3>
#include <ScrollBarConstants.au3>

_Main()

Func _Main()
	Local $nFileMenu, $nExititem, $GUIMsg, $hGUI, $h_cGUI, $h_cGUI2
	Local $listview, $button

	$hGUI = GUICreate("ScrollBar Example", 400, 400, -1, -1, BitOR($WS_MINIMIZEBOX, $WS_CAPTION, $WS_POPUP, $WS_SYSMENU, $WS_SIZEBOX))
	GUISetBkColor(0x88AABB)

	$nFileMenu = GUICtrlCreateMenu("File")
	$nExititem = GUICtrlCreateMenuItem("Exit", $nFileMenu)
	$listview = GUICtrlCreateListView("col1  |col2|col3  ", 10, 10, 200, 150);,$LVS_SORTDESCENDING)
	$button = GUICtrlCreateButton("Value?", 75, 170, 70, 20)
	GUICtrlSetResizing($button, $GUI_DOCKALL)
	For $x = 1 To 30
		GUICtrlCreateListViewItem("item" & $x & "|col2|col3", $listview)
	Next
	GUICtrlSetResizing($listview, $GUI_DOCKALL)

	$h_cGUI = GUICreate("Child GUI", 200, 200, 10, 200, $WS_CHILD, $WS_EX_CLIENTEDGE, $hGUI)
	GUICtrlCreateButton("a button", 10, 10, 90, 20)
	GUISetBkColor(0X006400)
	GUISetState()
	GUICtrlSetResizing($h_cGUI, $GUI_DOCKALL)

	GUISwitch($hGUI)

	$h_cGUI2 = GUICreate("Child GUI", 200, 200, 215, 10, $WS_CHILD, $WS_EX_CLIENTEDGE, $hGUI)
	GUICtrlCreateButton("a button", 10, 10, 90, 20)
	GUISetBkColor(0X006400)
	GUISetState()
	GUICtrlSetResizing($h_cGUI2, $GUI_DOCKALL)

	GUISwitch($hGUI)

	GUIRegisterMsg($WM_SIZE, "WM_SIZE")
	GUIRegisterMsg($WM_VSCROLL, "WM_VSCROLL")
	GUIRegisterMsg($WM_HSCROLL, "WM_HSCROLL")

	GUISetState()

	_GUIScrollBars_Init($hGUI)
	_GUIScrollBars_Init($h_cGUI)

	While 1
		$GUIMsg = GUIGetMsg()

		Switch $GUIMsg
			Case $GUI_EVENT_CLOSE, $nExititem
				ExitLoop
		EndSwitch
	WEnd

	Exit
EndFunc   ;==>_Main

Func WM_SIZE($hWnd, $Msg, $wParam, $lParam)
	#forceref $Msg, $wParam
	Local $index = -1, $yChar, $xChar, $xClientMax, $xClient, $yClient, $ivMax
	For $x = 0 To UBound($aSB_WindowInfo) - 1
		If $aSB_WindowInfo[$x][0] = $hWnd Then
			$index = $x
			$xClientMax = $aSB_WindowInfo[$index][1]
			$xChar = $aSB_WindowInfo[$index][2]
			$yChar = $aSB_WindowInfo[$index][3]
			$ivMax = $aSB_WindowInfo[$index][7]
			ExitLoop
		EndIf
	Next
	If $index = -1 Then Return 0

	Local $tSCROLLINFO = DllStructCreate($tagSCROLLINFO)

	; Retrieve the dimensions of the client area.
	$xClient = BitAND($lParam, 0x0000FFFF)
	$yClient = BitShift($lParam, 16)
	$aSB_WindowInfo[$index][4] = $xClient
	$aSB_WindowInfo[$index][5] = $yClient

	; Set the vertical scrolling range and page size
	DllStructSetData($tSCROLLINFO, "fMask", BitOR($SIF_RANGE, $SIF_PAGE))
	DllStructSetData($tSCROLLINFO, "nMin", 0)
	DllStructSetData($tSCROLLINFO, "nMax", $ivMax)
	DllStructSetData($tSCROLLINFO, "nPage", $yClient / $yChar)
	_GUIScrollBars_SetScrollInfo($hWnd, $SB_VERT, $tSCROLLINFO)

	; Set the horizontal scrolling range and page size
	DllStructSetData($tSCROLLINFO, "fMask", BitOR($SIF_RANGE, $SIF_PAGE))
	DllStructSetData($tSCROLLINFO, "nMin", 0)
	DllStructSetData($tSCROLLINFO, "nMax", 2 + $xClientMax / $xChar)
	DllStructSetData($tSCROLLINFO, "nPage", $xClient / $xChar)
	_GUIScrollBars_SetScrollInfo($hWnd, $SB_HORZ, $tSCROLLINFO)

	Return $GUI_RUNDEFMSG
EndFunc   ;==>WM_SIZE

Func WM_HSCROLL($hWnd, $Msg, $wParam, $lParam)
	#forceref $Msg, $lParam
	Local $nScrollCode = BitAND($wParam, 0x0000FFFF)

	Local $index = -1, $xChar, $xPos
	Local $Min, $Max, $Page, $Pos, $TrackPos

	For $x = 0 To UBound($aSB_WindowInfo) - 1
		If $aSB_WindowInfo[$x][0] = $hWnd Then
			$index = $x
			$xChar = $aSB_WindowInfo[$index][2]
			ExitLoop
		EndIf
	Next
	If $index = -1 Then Return 0

;~ 	; Get all the horizontal scroll bar information
	Local $tSCROLLINFO = _GUIScrollBars_GetScrollInfoEx($hWnd, $SB_HORZ)
	$Min = DllStructGetData($tSCROLLINFO, "nMin")
	$Max = DllStructGetData($tSCROLLINFO, "nMax")
	$Page = DllStructGetData($tSCROLLINFO, "nPage")
	; Save the position for comparison later on
	$xPos = DllStructGetData($tSCROLLINFO, "nPos")
	$Pos = $xPos
	$TrackPos = DllStructGetData($tSCROLLINFO, "nTrackPos")
	#forceref $Min, $Max
	Switch $nScrollCode

		Case $SB_LINELEFT ; user clicked left arrow
			DllStructSetData($tSCROLLINFO, "nPos", $Pos - 1)

		Case $SB_LINERIGHT ; user clicked right arrow
			DllStructSetData($tSCROLLINFO, "nPos", $Pos + 1)

		Case $SB_PAGELEFT ; user clicked the scroll bar shaft left of the scroll box
			DllStructSetData($tSCROLLINFO, "nPos", $Pos - $Page)

		Case $SB_PAGERIGHT ; user clicked the scroll bar shaft right of the scroll box
			DllStructSetData($tSCROLLINFO, "nPos", $Pos + $Page)

		Case $SB_THUMBTRACK ; user dragged the scroll box
			DllStructSetData($tSCROLLINFO, "nPos", $TrackPos)
	EndSwitch

;~    // Set the position and then retrieve it.  Due to adjustments
;~    //   by Windows it may not be the same as the value set.

	DllStructSetData($tSCROLLINFO, "fMask", $SIF_POS)
	_GUIScrollBars_SetScrollInfo($hWnd, $SB_HORZ, $tSCROLLINFO)
	_GUIScrollBars_GetScrollInfo($hWnd, $SB_HORZ, $tSCROLLINFO)
	;// If the position has changed, scroll the window and update it
	$Pos = DllStructGetData($tSCROLLINFO, "nPos")
	If ($Pos <> $xPos) Then _GUIScrollBars_ScrollWindow($hWnd, $xChar * ($xPos - $Pos), 0)
	Return $GUI_RUNDEFMSG
EndFunc   ;==>WM_HSCROLL

Func WM_VSCROLL($hWnd, $Msg, $wParam, $lParam)
	#forceref $Msg, $wParam, $lParam
	Local $nScrollCode = BitAND($wParam, 0x0000FFFF)
	Local $index = -1, $yChar, $yPos
	Local $Min, $Max, $Page, $Pos, $TrackPos

	For $x = 0 To UBound($aSB_WindowInfo) - 1
		If $aSB_WindowInfo[$x][0] = $hWnd Then
			$index = $x
			$yChar = $aSB_WindowInfo[$index][3]
			ExitLoop
		EndIf
	Next
	If $index = -1 Then Return 0


	; Get all the vertial scroll bar information
	Local $tSCROLLINFO = _GUIScrollBars_GetScrollInfoEx($hWnd, $SB_VERT)
	$Min = DllStructGetData($tSCROLLINFO, "nMin")
	$Max = DllStructGetData($tSCROLLINFO, "nMax")
	$Page = DllStructGetData($tSCROLLINFO, "nPage")
	; Save the position for comparison later on
	$yPos = DllStructGetData($tSCROLLINFO, "nPos")
	$Pos = $yPos
	$TrackPos = DllStructGetData($tSCROLLINFO, "nTrackPos")

	Switch $nScrollCode
		Case $SB_TOP ; user clicked the HOME keyboard key
			DllStructSetData($tSCROLLINFO, "nPos", $Min)

		Case $SB_BOTTOM ; user clicked the END keyboard key
			DllStructSetData($tSCROLLINFO, "nPos", $Max)

		Case $SB_LINEUP ; user clicked the top arrow
			DllStructSetData($tSCROLLINFO, "nPos", $Pos - 1)

		Case $SB_LINEDOWN ; user clicked the bottom arrow
			DllStructSetData($tSCROLLINFO, "nPos", $Pos + 1)

		Case $SB_PAGEUP ; user clicked the scroll bar shaft above the scroll box
			DllStructSetData($tSCROLLINFO, "nPos", $Pos - $Page)

		Case $SB_PAGEDOWN ; user clicked the scroll bar shaft below the scroll box
			DllStructSetData($tSCROLLINFO, "nPos", $Pos + $Page)

		Case $SB_THUMBTRACK ; user dragged the scroll box
			DllStructSetData($tSCROLLINFO, "nPos", $TrackPos)
	EndSwitch

;~    // Set the position and then retrieve it.  Due to adjustments
;~    //   by Windows it may not be the same as the value set.

	DllStructSetData($tSCROLLINFO, "fMask", $SIF_POS)
	_GUIScrollBars_SetScrollInfo($hWnd, $SB_VERT, $tSCROLLINFO)
	_GUIScrollBars_GetScrollInfo($hWnd, $SB_VERT, $tSCROLLINFO)
	;// If the position has changed, scroll the window and update it
	$Pos = DllStructGetData($tSCROLLINFO, "nPos")

	If ($Pos <> $yPos) Then
		_GUIScrollBars_ScrollWindow($hWnd, 0, $yChar * ($yPos - $Pos))
		$yPos = $Pos
	EndIf

	Return $GUI_RUNDEFMSG

EndFunc   ;==>WM_VSCROLL
