#include <GUIConstantsEx.au3>
#include <GDIPlus.au3>
#include <WindowsConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $hBrush1, $hBrush2

	; Create GUI
	GUICreate("GDI+", 400, 300)
	$iMemo = GUICtrlCreateEdit("", 2, 2, 596, 396, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Create brushes
	_GDIPlus_Startup()
	$hBrush1 = _GDIPlus_BrushCreateSolid()
	$hBrush2 = _GDIPlus_BrushClone($hBrush1)

	; Show brush information
	MemoWrite("Brush 1 handle : 0x" & Hex($hBrush1))
	MemoWrite("Brush 1 type ..: " & _GDIPlus_BrushGetType($hBrush1))
	MemoWrite("Brush 2 handle : 0x" & Hex($hBrush2))
	MemoWrite("Brush 2 type ..: " & _GDIPlus_BrushGetType($hBrush2))

	; Clean up resources
	_GDIPlus_BrushDispose($hBrush2)
	_GDIPlus_BrushDispose($hBrush1)
	_GDIPlus_Shutdown()

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage = '')
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
