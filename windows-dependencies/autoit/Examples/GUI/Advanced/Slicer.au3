#include <GDIPlus.au3>
#include <ScreenCapture.au3>
#include <WinAPI.au3>
#include <GuiConstantsEx.au3>

; ===============================================================================================================================
; Description ...: Shows how to slice up an image and then put it back together
; Author ........: Paul Campbell (PaulIA)
; Notes .........:
; ===============================================================================================================================

; ===============================================================================================================================
; Global variables
; ===============================================================================================================================
Global $iI, $hBitmap, $hGraphic, $hGUI, $hImage, $aSlice[4]

; ===============================================================================================================================
; Main
; ===============================================================================================================================

; Capture screen region
$hBitmap = _ScreenCapture_Capture("", 0, 0, 400, 400)

; Create GUI
$hGUI = GUICreate("Slicer", 400, 400)
GUISetState()

; Initialize GDI+ library
_GDIPlus_Startup()

; Slice up screen capture into 4 equal parts
$hImage = _GDIPlus_BitmapCreateFromHBITMAP($hBitmap)
$aSlice[0] = _GDIPlus_BitmapCloneArea($hImage, 0, 0, 200, 200)
$aSlice[1] = _GDIPlus_BitmapCloneArea($hImage, 200, 0, 200, 200)
$aSlice[2] = _GDIPlus_BitmapCloneArea($hImage, 0, 200, 200, 200)
$aSlice[3] = _GDIPlus_BitmapCloneArea($hImage, 200, 200, 200, 200)

; Show each slice
$hGraphic = _GDIPlus_GraphicsCreateFromHWND($hGUI)
For $iI = 0 To 3
	_GDIPlus_GraphicsDrawImage($hGraphic, $aSlice[$iI], 100, 100)
	Sleep(2000)
Next

; Stitch slices back together again and display
_GDIPlus_GraphicsDrawImage($hGraphic, $aSlice[0], 0, 0)
_GDIPlus_GraphicsDrawImage($hGraphic, $aSlice[1], 200, 0)
_GDIPlus_GraphicsDrawImage($hGraphic, $aSlice[2], 0, 200)
_GDIPlus_GraphicsDrawImage($hGraphic, $aSlice[3], 200, 200)

; Loop until user exits
Do
Until GUIGetMsg() = $GUI_EVENT_CLOSE

; Clean up resources
_GDIPlus_GraphicsDispose($hGraphic)
_GDIPlus_ImageDispose($aSlice[0])
_GDIPlus_ImageDispose($aSlice[1])
_GDIPlus_ImageDispose($aSlice[2])
_GDIPlus_ImageDispose($aSlice[3])
_GDIPlus_GraphicsDispose($hImage)
_WinAPI_DeleteObject($hBitmap)

; Shut down GDI+ library
_GDIPlus_Shutdown()
