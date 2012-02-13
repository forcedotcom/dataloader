#include <GDIPlus.au3>
#include <GuiConstantsEx.au3>

; ===============================================================================================================================
; Description ...: Shows how to display a PNG image
; Author ........: Paul Campbell (PaulIA)
; Notes .........:
; ===============================================================================================================================

; ===============================================================================================================================
; Global variables
; ===============================================================================================================================
Global $hGUI, $hImage, $hGraphic

; Create GUI
$hGUI = GUICreate("Show PNG", 240, 240)
GUISetState()

; Load PNG image
_GDIPlus_Startup()
$hImage = _GDIPlus_ImageLoadFromFile("..\Torus.png")

; Draw PNG image
$hGraphic = _GDIPlus_GraphicsCreateFromHWND($hGUI)
_GDIPlus_GraphicsDrawImage($hGraphic, $hImage, 0, 0)

; Loop until user exits
Do
Until GUIGetMsg() = $GUI_EVENT_CLOSE

; Clean up resources
_GDIPlus_GraphicsDispose($hGraphic)
_GDIPlus_ImageDispose($hImage)
_GDIPlus_Shutdown()
