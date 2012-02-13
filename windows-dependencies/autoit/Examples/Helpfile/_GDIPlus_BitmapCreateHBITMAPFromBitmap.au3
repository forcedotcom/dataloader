#include <GUIConstantsEx.au3>
#include <GDIPlus.au3>
#include <ScreenCapture.au3>

_Main()

Func _Main()
	Local $hBMP, $hImage, $iX, $iY, $hClone

	; Initialize GDI+ library
	_GDIPlus_Startup()

	; Capture 32 bit bitmap
	$hBMP = _ScreenCapture_Capture("")
	$hImage = _GDIPlus_BitmapCreateFromHBITMAP($hBMP)

	; Create 24 bit bitmap clone
	$iX = _GDIPlus_ImageGetWidth($hImage)
	$iY = _GDIPlus_ImageGetHeight($hImage)
	$hClone = _GDIPlus_BitmapCloneArea($hImage, 0, 0, $iX, $iY, $GDIP_PXF24RGB)

	; Save bitmap to file
	_GDIPlus_ImageSaveToFile($hClone, @TempDir & "\GDIPlus_Image.bmp")

	; Clean up resources
	_GDIPlus_BitmapDispose($hClone)
	_GDIPlus_BitmapDispose($hImage)
	_WinAPI_DeleteObject($hBMP)

	; Load image
	$hImage = _GDIPlus_ImageLoadFromFile(@TempDir & "\GDIPlus_Image.bmp")
	$hBMP = _GDIPlus_BitmapCreateHBITMAPFromBitmap($hImage)

	; Save bitmap to file
	_ScreenCapture_SaveImage(@TempDir & "\Image.bmp", $hBMP, True) ; True -> $hBMP destroyed

	; Clean up resource
	_GDIPlus_ImageDispose($hImage)

	; Shut down GDI+ library
	_GDIPlus_Shutdown()

EndFunc   ;==>_Main
