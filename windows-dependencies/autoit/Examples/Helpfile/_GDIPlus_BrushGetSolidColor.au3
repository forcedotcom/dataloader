#include <GUIConstantsEx.au3>
#include <GDIPlus.au3>
#include <WindowsConstants.au3>

_Main()

Func _Main()
	Local $hGUI, $Label1, $label2, $hGraphic, $hBrush1, $iClr1, $iClr2

	; Create GUI
	$hGUI = GUICreate("GDI+", 345, 150)
	$Label1 = GUICtrlCreateLabel("", 2, 2, 150, 20)
	$label2 = GUICtrlCreateLabel("", 202, 2, 150, 20)
	GUISetState()
	Sleep(100)

	; Start GDIPlus
	_GDIPlus_Startup()
	$hGraphic = _GDIPlus_GraphicsCreateFromHWND($hGUI)

	; Create solid brush
	$hBrush1 = _GDIPlus_BrushCreateSolid()

	; Get solid brush color
	$iClr1 = _GDIPlus_BrushGetSolidColor($hBrush1)

	; Draw some graphics with the original brush color
	_GDIPlus_GraphicsFillEllipse($hGraphic, 25, 25, 100, 100, $hBrush1)

	; Set new brush color (0xFFFF0000 = Red)
	_GDIPlus_BrushSetSolidColor($hBrush1, 0xFFFF0000)

	; Get new solid brush color
	$iClr2 = _GDIPlus_BrushGetSolidColor($hBrush1)

	; Draw some graphics with the new brush color
	_GDIPlus_GraphicsFillRect($hGraphic, 220, 25, 100, 100, $hBrush1)

	; Write original brush color to Label1
	GUICtrlSetData($Label1, "Brush orignal color: " & Hex($iClr1))

	; Write the new brush color to Label2
	GUICtrlSetData($label2, "Brush new color: " & Hex($iClr2))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

	; Clean up resources
	_GDIPlus_BrushDispose($hBrush1)
	_GDIPlus_GraphicsDispose($hGraphic)
	_GDIPlus_Shutdown()

EndFunc   ;==>_Main
