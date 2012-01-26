#include <GDIPlus.au3>
#include <ScreenCapture.au3>
#include <WinAPI.au3>
#include <GuiConstantsEx.au3>

; ===============================================================================================================================
; Description ...: Shows how to magnify an image
; Author ........: Paul Campbell (PaulIA)
; Notes .........:
; ===============================================================================================================================

; ===============================================================================================================================
; Global variables
; ===============================================================================================================================

Global $hBMP, $hGUI1, $hGUI2, $hBitmap, $hGraphic1, $hGraphic2

; ===============================================================================================================================
; Main
; ===============================================================================================================================

; Capture top left corner of the screen
$hBMP = _ScreenCapture_Capture("", 0, 0, 400, 300)

; Create a GUI for the original image
$hGUI1 = GUICreate("Original", 400, 300, 0, 0)
GUISetState()

; Create a GUI for the zoomed image
$hGUI2 = GUICreate("Zoomed", 400, 300, 0, 400)
GUISetState()

; Initialize GDI+ library and load image
_GDIPlus_Startup()
$hBitmap = _GDIPlus_BitmapCreateFromHBITMAP($hBMP)

; Draw original image
$hGraphic1 = _GDIPlus_GraphicsCreateFromHWND($hGUI1)
_GDIPlus_GraphicsDrawImage($hGraphic1, $hBitmap, 0, 0)

; Draw 2x zoomed image
$hGraphic2 = _GDIPlus_GraphicsCreateFromHWND($hGUI2)
_GDIPlus_GraphicsDrawImageRectRect($hGraphic2, $hBitmap, 0, 0, 200, 200, 0, 0, 400, 300)

; Release resources
_GDIPlus_GraphicsDispose($hGraphic1)
_GDIPlus_GraphicsDispose($hGraphic2)
_GDIPlus_ImageDispose($hBitmap)
_WinAPI_DeleteObject($hBMP)
_GDIPlus_Shutdown()

; Loop until user exits
Do
Until GUIGetMsg() = $GUI_EVENT_CLOSE
