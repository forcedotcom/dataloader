#include <GuiMenu.au3>

_Main()

Func _Main()
	Local $hWnd, $hMain, $hFile, $tRect, $tPoint, $iX, $iY, $iIndex

	; Open Notepad
	Run("notepad.exe")
	WinWaitActive("[CLASS:Notepad]")
	$hWnd = WinGetHandle("[CLASS:Notepad]")
	$hMain = _GUICtrlMenu_GetMenu($hWnd)
	$hFile = _GUICtrlMenu_GetItemSubMenu($hMain, 0)

	; Open File menu
	Send("!f")
	Sleep(1000)

	; Move mouse over Open menu item
	$tRect = _GUICtrlMenu_GetItemRectEx($hWnd, $hFile, 1)
	$tPoint = _Lib_PointFromRect($tRect, True)
	_Lib_GetXYFromPoint($tPoint, $iX, $iY)
	MouseMove($iX, $iY, 1)
	Sleep(1000)

	; Get menu item from current mouse position
	$iIndex = _GUICtrlMenu_MenuItemFromPoint($hWnd, $hFile)
	Send("{ESC 2}")
	Writeln("Menu item under cursor was: " & $iIndex)

EndFunc   ;==>_Main

; Write a line of text to Notepad
Func Writeln($sText)
	ControlSend("[CLASS:Notepad]", "", "Edit1", $sText & @CR)
EndFunc   ;==>Writeln

Func _Lib_PointFromRect(ByRef $tRect, $fCenter = True)
	Local $iX1, $iY1, $iX2, $iY2, $tPoint

	$iX1 = DllStructGetData($tRect, "Left")
	$iY1 = DllStructGetData($tRect, "Top")
	$iX2 = DllStructGetData($tRect, "Right")
	$iY2 = DllStructGetData($tRect, "Bottom")
	If $fCenter Then
		$iX1 = $iX1 + (($iX2 - $iX1) / 2)
		$iY1 = $iY1 + (($iY2 - $iY1) / 2)
	EndIf
	$tPoint = DllStructCreate($tagPOINT)
	DllStructSetData($tPoint, "X", $iX1)
	DllStructSetData($tPoint, "Y", $iY1)
	Return $tPoint
EndFunc   ;==>_Lib_PointFromRect

Func _Lib_GetXYFromPoint(ByRef $tPoint, ByRef $iX, ByRef $iY)
	$iX = DllStructGetData($tPoint, "X")
	$iY = DllStructGetData($tPoint, "Y")
EndFunc   ;==>_Lib_GetXYFromPoint
