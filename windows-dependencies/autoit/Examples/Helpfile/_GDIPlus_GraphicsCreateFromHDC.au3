#include <GUIConstantsEx.au3>
#include <GDIPlus.au3>
#include <WinAPI.au3>

_Main()

Func _Main()
	Local $hGUI, $hGraphic, $hBrush, $hFormat, $hFamily, $hFont, $tLayout, $hDC

	; Create GUI
	$hGUI = GUICreate("GDI+", 400, 300)
	$hDC = _WinAPI_GetWindowDC($hGUI)
	GUISetState()

	; Draw a string
	_GDIPlus_Startup()
	$hGraphic = _GDIPlus_GraphicsCreateFromHDC($hDC)
	$hBrush = _GDIPlus_BrushCreateSolid(0x7F00007F)
	$hFormat = _GDIPlus_StringFormatCreate()
	$hFamily = _GDIPlus_FontFamilyCreate("Arial")
	$hFont = _GDIPlus_FontCreate($hFamily, 12, 2)
	$tLayout = _GDIPlus_RectFCreate(140, 110, 100, 20)
	_GDIPlus_GraphicsDrawStringEx($hGraphic, "Hello world", $hFont, $tLayout, $hFormat, $hBrush)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

	; Clean up resources
	_GDIPlus_FontDispose($hFont)
	_GDIPlus_FontFamilyDispose($hFamily)
	_GDIPlus_StringFormatDispose($hFormat)
	_GDIPlus_BrushDispose($hBrush)
	_GDIPlus_GraphicsDispose($hGraphic)
	_WinAPI_ReleaseDC($hGUI, $hDC)
	_GDIPlus_Shutdown()

EndFunc   ;==>_Main
