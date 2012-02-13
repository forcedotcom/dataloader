#include <GUIConstantsEx.au3>
#include <GuiListView.au3>
#include <GuiImageList.au3>
#include <WindowsConstants.au3>

Opt("GUIOnEventMode", 1)
Global $hListView, $hListView2, $GUI1, $GUI2, $run = 1
Global $Dock = 1, $Dock_Location = 1, $x1, $x2, $y1, $y2
Global $OptionsItem2, $OptionsItem3, $OptionsItem4
Global $Options2Item2, $Options2Item3, $Options2Item4

_Main()

Func _Main()
	$run = 1
	Local $OptionsMenu, $OptionsItem1, $Options2Menu, $Options2Item1, $width = 450
	Local $Btn_MoveLeft, $Btn_MoveRight, $Btn_Exit1, $Btn_Exit2
	Local $Btn_CopyRight, $Btn_CopyLeft, $hImage
	Local $exStyles = BitOR($LVS_EX_GRIDLINES, $LVS_EX_FULLROWSELECT, $LVS_EX_CHECKBOXES, $LVS_EX_SUBITEMIMAGES)

	$GUI1 = GUICreate("(External) ListView Copy Items", $width, 300, 10, 10)
	$OptionsMenu = GUICtrlCreateMenu("Options")
	$OptionsItem1 = GUICtrlCreateMenu("Docking", $OptionsMenu)

	$OptionsItem2 = GUICtrlCreateMenuItem("Docked", $OptionsItem1)
	GUICtrlCreateMenuItem("", $OptionsItem1)
	$OptionsItem3 = GUICtrlCreateMenuItem("Side By Side", $OptionsItem1)
	$OptionsItem4 = GUICtrlCreateMenuItem("Top And Bottom", $OptionsItem1)
	GUICtrlSetState($OptionsItem2, $GUI_CHECKED)
	GUICtrlSetState($OptionsItem3, $GUI_CHECKED)
	GUICtrlSetOnEvent($OptionsItem2, "_SetDocking")
	GUICtrlSetOnEvent($OptionsItem3, "_SetDockSideBySide")
	GUICtrlSetOnEvent($OptionsItem4, "_SetDockTopAndBottom")

	GUISetOnEvent($GUI_EVENT_CLOSE, "SpecialEvents")
	GUISetOnEvent($GUI_EVENT_MINIMIZE, "SpecialEvents")
	GUISetOnEvent($GUI_EVENT_RESTORE, "SpecialEvents")

	; need the handle because UDF function(s) are used to create the items
	$hListView = GUICtrlGetHandle(GUICtrlCreateListView("", 2, 2, $width - 100, 268, BitOR($LVS_REPORT, $LVS_SHOWSELALWAYS), $WS_EX_CLIENTEDGE))
	_GUICtrlListView_SetExtendedListViewStyle($hListView, $exStyles)

	$Btn_MoveRight = GUICtrlCreateButton("Move", $width - 95, 35, 90, 20)
	GUICtrlSetOnEvent($Btn_MoveRight, "_MoveRight")

	$Btn_CopyRight = GUICtrlCreateButton("Copy", $width - 95, 60, 90, 20)
	GUICtrlSetOnEvent($Btn_CopyRight, "_CopyRight")

	$Btn_Exit1 = GUICtrlCreateButton("Exit", $width - 95, 140, 90, 25)
	GUICtrlSetOnEvent($Btn_Exit1, "_Exit")

	$GUI2 = GUICreate("Right/Bottom Window", $width, 300, $width + 15, 10)

	$Options2Menu = GUICtrlCreateMenu("Options")
	$Options2Item1 = GUICtrlCreateMenu("Docking", $Options2Menu)
	$Options2Item2 = GUICtrlCreateMenuItem("Docked", $Options2Item1)
	GUICtrlCreateMenuItem("", $Options2Item1)
	$Options2Item3 = GUICtrlCreateMenuItem("Side By Side", $Options2Item1)
	$Options2Item4 = GUICtrlCreateMenuItem("Top And Bottom", $Options2Item1)
	GUICtrlSetState($Options2Item2, $GUI_CHECKED)
	GUICtrlSetState($Options2Item3, $GUI_CHECKED)
	GUICtrlSetOnEvent($Options2Item2, "_SetDocking2")
	GUICtrlSetOnEvent($Options2Item3, "_SetDockSideBySide2")
	GUICtrlSetOnEvent($Options2Item4, "_SetDockTopAndBottom2")

	GUISetOnEvent($GUI_EVENT_CLOSE, "SpecialEvents")
	GUISetOnEvent($GUI_EVENT_MINIMIZE, "SpecialEvents")
	GUISetOnEvent($GUI_EVENT_RESTORE, "SpecialEvents")

	$hListView2 = GUICtrlGetHandle(GUICtrlCreateListView("", 2, 2, $width - 100, 268, BitOR($LVS_REPORT, $LVS_SHOWSELALWAYS), $WS_EX_CLIENTEDGE))
	_GUICtrlListView_SetExtendedListViewStyle($hListView2, $exStyles)

	$Btn_MoveLeft = GUICtrlCreateButton("Move", $width - 95, 35, 90, 20)
	GUICtrlSetOnEvent($Btn_MoveLeft, "_MoveLeft")

	$Btn_CopyLeft = GUICtrlCreateButton("Copy", $width - 95, 60, 90, 20)
	GUICtrlSetOnEvent($Btn_CopyLeft, "_CopyLeft")

	$Btn_Exit2 = GUICtrlCreateButton("Exit", $width - 95, 140, 90, 25)
	GUICtrlSetOnEvent($Btn_Exit2, "_Exit")

	GUISetState(@SW_SHOW, $GUI2)
	GUISetState(@SW_SHOW, $GUI1)

	; Load images
	$hImage = _GUIImageList_Create(16, 16, 5, 3)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 13)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 14)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 15)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 16)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 17)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 18)
	_GUICtrlListView_SetImageList($hListView, $hImage, 1)
	_GUICtrlListView_SetImageList($hListView2, $hImage, 1)

	; Add columns
	_GUICtrlListView_AddColumn($hListView, "Column 1", 120)
	_GUICtrlListView_AddColumn($hListView, "Column 2", 100)
	_GUICtrlListView_AddColumn($hListView, "Column 3", 100)

	; Add columns
	_GUICtrlListView_AddColumn($hListView2, "Column 1", 120)
	_GUICtrlListView_AddColumn($hListView2, "Column 2", 100)
	_GUICtrlListView_AddColumn($hListView2, "Column 3", 100)

	; Add items
	_GUICtrlListView_AddItem($hListView, "Row 1: Col 1", 0)
	_GUICtrlListView_AddSubItem($hListView, 0, "Row 1: Col 2", 1, 1)
	_GUICtrlListView_AddSubItem($hListView, 0, "Row 1: Col 3", 2, 2)
	_GUICtrlListView_AddItem($hListView, "Row 2: Col 1", 1)
	_GUICtrlListView_AddSubItem($hListView, 1, "Row 2: Col 2", 1, 2)
	_GUICtrlListView_AddItem($hListView, "Row 3: Col 1", 2)
	_GUICtrlListView_AddItem($hListView, "Row 4: Col 1", 3)
	_GUICtrlListView_AddItem($hListView, "Row 5: Col 1", 4)
	_GUICtrlListView_AddSubItem($hListView, 4, "Row 5: Col 2", 1, 3)
	_GUICtrlListView_AddItem($hListView, "Row 6: Col 1", 5)
	_GUICtrlListView_AddSubItem($hListView, 5, "Row 6: Col 2", 1, 4)
	_GUICtrlListView_AddSubItem($hListView, 5, "Row 6: Col 3", 2, 3)

	While $run
		If $Dock Then _KeepWindowsDocked()
		Sleep(10)
	WEnd
EndFunc   ;==>_Main

Func _SetDocking()
	If BitAND(GUICtrlRead($OptionsItem2), $GUI_CHECKED) = $GUI_CHECKED Then
		GUICtrlSetState($OptionsItem2, $GUI_UNCHECKED)
		GUICtrlSetState($Options2Item2, $GUI_UNCHECKED)
		$Dock = 0
	Else
		GUICtrlSetState($OptionsItem2, $GUI_CHECKED)
		GUICtrlSetState($Options2Item2, $GUI_CHECKED)
		$Dock = 2
	EndIf
	If $Dock Then _KeepWindowsDocked()
EndFunc   ;==>_SetDocking

Func _SetDocking2()
	If BitAND(GUICtrlRead($Options2Item2), $GUI_CHECKED) = $GUI_CHECKED Then
		GUICtrlSetState($OptionsItem2, $GUI_UNCHECKED)
		GUICtrlSetState($Options2Item2, $GUI_UNCHECKED)
		$Dock = 0
	Else
		GUICtrlSetState($OptionsItem2, $GUI_CHECKED)
		GUICtrlSetState($Options2Item2, $GUI_CHECKED)
		$Dock = 2
	EndIf
	If $Dock Then _KeepWindowsDocked()
EndFunc   ;==>_SetDocking2

Func _SetDockSideBySide()
	If BitAND(GUICtrlRead($OptionsItem3), $GUI_CHECKED) = $GUI_CHECKED Then
		GUICtrlSetState($OptionsItem3, $GUI_UNCHECKED)
		GUICtrlSetState($OptionsItem4, $GUI_CHECKED)
		GUICtrlSetState($Options2Item3, $GUI_UNCHECKED)
		GUICtrlSetState($Options2Item4, $GUI_CHECKED)
		$Dock_Location = 2
	Else
		GUICtrlSetState($OptionsItem3, $GUI_CHECKED)
		GUICtrlSetState($OptionsItem4, $GUI_UNCHECKED)
		GUICtrlSetState($Options2Item3, $GUI_CHECKED)
		GUICtrlSetState($Options2Item4, $GUI_UNCHECKED)
		$Dock_Location = 1
		If $Dock Then $Dock = 2
	EndIf
	If $Dock Then _KeepWindowsDocked()
EndFunc   ;==>_SetDockSideBySide

Func _SetDockSideBySide2()
	If BitAND(GUICtrlRead($Options2Item3), $GUI_CHECKED) = $GUI_CHECKED Then
		GUICtrlSetState($OptionsItem3, $GUI_UNCHECKED)
		GUICtrlSetState($OptionsItem4, $GUI_CHECKED)
		GUICtrlSetState($Options2Item3, $GUI_UNCHECKED)
		GUICtrlSetState($Options2Item4, $GUI_CHECKED)
		$Dock_Location = 2
	Else
		GUICtrlSetState($OptionsItem3, $GUI_CHECKED)
		GUICtrlSetState($OptionsItem4, $GUI_UNCHECKED)
		GUICtrlSetState($Options2Item3, $GUI_CHECKED)
		GUICtrlSetState($Options2Item4, $GUI_UNCHECKED)
		$Dock_Location = 1
		If $Dock Then $Dock = 2
	EndIf
	If $Dock Then _KeepWindowsDocked()
EndFunc   ;==>_SetDockSideBySide2

Func _SetDockTopAndBottom()
	If BitAND(GUICtrlRead($OptionsItem4), $GUI_CHECKED) = $GUI_CHECKED Then
		GUICtrlSetState($OptionsItem4, $GUI_UNCHECKED)
		GUICtrlSetState($OptionsItem3, $GUI_CHECKED)
		GUICtrlSetState($Options2Item4, $GUI_UNCHECKED)
		GUICtrlSetState($Options2Item3, $GUI_CHECKED)
		$Dock_Location = 1
	Else
		GUICtrlSetState($OptionsItem4, $GUI_CHECKED)
		GUICtrlSetState($OptionsItem3, $GUI_UNCHECKED)
		GUICtrlSetState($Options2Item4, $GUI_CHECKED)
		GUICtrlSetState($Options2Item3, $GUI_UNCHECKED)
		$Dock_Location = 2
		If $Dock Then $Dock = 2
	EndIf
	If $Dock Then _KeepWindowsDocked()
EndFunc   ;==>_SetDockTopAndBottom

Func _SetDockTopAndBottom2()
	If BitAND(GUICtrlRead($Options2Item4), $GUI_CHECKED) = $GUI_CHECKED Then
		GUICtrlSetState($OptionsItem4, $GUI_UNCHECKED)
		GUICtrlSetState($OptionsItem3, $GUI_CHECKED)
		GUICtrlSetState($Options2Item4, $GUI_UNCHECKED)
		GUICtrlSetState($Options2Item3, $GUI_CHECKED)
		$Dock_Location = 1
	Else
		GUICtrlSetState($OptionsItem4, $GUI_CHECKED)
		GUICtrlSetState($OptionsItem3, $GUI_UNCHECKED)
		GUICtrlSetState($Options2Item4, $GUI_CHECKED)
		GUICtrlSetState($Options2Item3, $GUI_UNCHECKED)
		$Dock_Location = 2
		If $Dock Then $Dock = 2
	EndIf
	If $Dock Then _KeepWindowsDocked()
EndFunc   ;==>_SetDockTopAndBottom2

Func _KeepWindowsDocked()
	Local $p_win1 = WinGetPos($GUI1)
	Local $p_win2 = WinGetPos($GUI2)
	If $Dock_Location == 1 Then
		If (($p_win1[0] <> $x1 Or $p_win1[1] <> $y1) And BitAND(WinGetState($GUI1), 8) Or $Dock = 2) Then
			$x1 = $p_win1[0]
			$y1 = $p_win1[1]
			$x2 = $p_win1[2] + $x1
			$y2 = $y1
			WinMove($GUI2, "", $x2, $y2)
			$Dock = 1
		ElseIf (($p_win2[0] <> $x2 Or $p_win2[1] <> $y2) And BitAND(WinGetState($GUI2), 8)) Then
			$x2 = $p_win2[0]
			$y2 = $p_win2[1]
			$x1 = $p_win2[0] - $p_win1[2]
			$y1 = $y2
			WinMove($GUI1, "", $x1, $y1)
		EndIf
	Else
		If (($p_win1[0] <> $x1 Or $p_win1[1] <> $y1) And BitAND(WinGetState($GUI1), 8) Or $Dock = 2) Then
			$x1 = $p_win1[0]
			$y1 = $p_win1[1]
			$x2 = $x1
			$y2 = $p_win1[3] + $y1
			WinMove($GUI2, "", $x2, $y2)
			$Dock = 1
		ElseIf (($p_win2[0] <> $x2 Or $p_win2[1] <> $y2) And BitAND(WinGetState($GUI2), 8)) Then
			$x2 = $p_win2[0]
			$y2 = $p_win2[1]
			$x1 = $x2
			$y1 = $p_win2[1] - $p_win1[3]
			WinMove($GUI1, "", $x1, $y1)
		EndIf
	EndIf
EndFunc   ;==>_KeepWindowsDocked

Func _CopyRight()
	_GUICtrlListView_CopyItems($hListView, $hListView2)
EndFunc   ;==>_CopyRight

Func _MoveRight()
	_GUICtrlListView_CopyItems($hListView, $hListView2, 1)
EndFunc   ;==>_MoveRight

Func _CopyLeft()
	_GUICtrlListView_CopyItems($hListView2, $hListView)
EndFunc   ;==>_CopyLeft

Func _MoveLeft()
	_GUICtrlListView_CopyItems($hListView2, $hListView, 1)
EndFunc   ;==>_MoveLeft

Func _Exit()
	$run = 0
	GUIDelete($GUI2)
	GUIDelete($GUI1)
EndFunc   ;==>_Exit

Func SpecialEvents()
	Select
		Case @GUI_CtrlId = $GUI_EVENT_CLOSE
			_Exit()
	EndSelect
EndFunc   ;==>SpecialEvents
