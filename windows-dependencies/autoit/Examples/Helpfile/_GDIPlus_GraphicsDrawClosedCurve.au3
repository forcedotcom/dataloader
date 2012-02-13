#include <GUIConstantsEx.au3>
#include <GDIPlus.au3>

_Main()

Func _Main()
	Local $hGUI, $hGraphic, $aPoints[8][2]

	; Create GUI
	$hGUI = GUICreate("GDI+", 400, 300)
	GUISetState()

	; Draw a cardinal spline
	_GDIPlus_Startup()
	$hGraphic = _GDIPlus_GraphicsCreateFromHWND($hGUI)

	$aPoints[0][0] = 7
	$aPoints[1][0] = 50
	$aPoints[1][1] = 50
	$aPoints[2][0] = 100
	$aPoints[2][1] = 25
	$aPoints[3][0] = 200
	$aPoints[3][1] = 5
	$aPoints[4][0] = 250
	$aPoints[4][1] = 50
	$aPoints[5][0] = 300
	$aPoints[5][1] = 100
	$aPoints[6][0] = 350
	$aPoints[6][1] = 200
	$aPoints[7][0] = 250
	$aPoints[7][1] = 250

	_GDIPlus_GraphicsDrawClosedCurve($hGraphic, $aPoints)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

	; Clean up resources
	_GDIPlus_GraphicsDispose($hGraphic)
	_GDIPlus_Shutdown()

EndFunc   ;==>_Main
