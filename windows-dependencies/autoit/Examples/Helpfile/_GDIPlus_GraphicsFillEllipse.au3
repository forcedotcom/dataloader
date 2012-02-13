#include <GUIConstantsEx.au3>
#include <GDIPlus.au3>

_Main()

Func _Main()
	Local $hGUI, $hGraphic

	; Create GUI
	$hGUI = GUICreate("GDI+", 400, 300)
	GUISetState()

	; Draw an ellipse
	_GDIPlus_Startup()
	$hGraphic = _GDIPlus_GraphicsCreateFromHWND($hGUI)
	_GDIPlus_GraphicsFillEllipse($hGraphic, 130, 100, 140, 70)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

	; Clean up resources
	_GDIPlus_GraphicsDispose($hGraphic)
	_GDIPlus_Shutdown()

EndFunc   ;==>_Main
