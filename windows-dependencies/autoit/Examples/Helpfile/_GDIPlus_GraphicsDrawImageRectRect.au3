#include <GUIConstantsEx.au3>
#include <GDIPlus.au3>
#include <ScreenCapture.au3>

_Main()

Func _Main()
	Local $hGUI1, $hGUI2, $hImage, $hGraphic1, $hGraphic2

	; Capture top left corner of the screen
	_ScreenCapture_Capture(@MyDocumentsDir & "\GDIPlus_Image.jpg", 0, 0, 400, 300)

	; Create a GUI for the original image
	$hGUI1 = GUICreate("Original", 400, 300, 0, 0)
	GUISetState()

	; Create a GUI for the zoomed image
	$hGUI2 = GUICreate("Zoomed", 400, 300, 0, 400)
	GUISetState()

	; Initialize GDI+ library and load image
	_GDIPlus_Startup()
	$hImage = _GDIPlus_ImageLoadFromFile(@MyDocumentsDir & "\GDIPlus_Image.jpg")

	; Draw original image
	$hGraphic1 = _GDIPlus_GraphicsCreateFromHWND($hGUI1)
	_GDIPlus_GraphicsDrawImage($hGraphic1, $hImage, 0, 0)

	; Draw 2x zoomed image
	$hGraphic2 = _GDIPlus_GraphicsCreateFromHWND($hGUI2)
	_GDIPlus_GraphicsDrawImageRectRect($hGraphic2, $hImage, 0, 0, 200, 200, 0, 0, 400, 300)

	; Release resources
	_GDIPlus_GraphicsDispose($hGraphic1)
	_GDIPlus_GraphicsDispose($hGraphic2)
	_GDIPlus_ImageDispose($hImage)
	_GDIPlus_Shutdown()

	; Clean up screen shot file
	FileDelete(@MyDocumentsDir & "\GDIPlus_Image.jpg")

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE


EndFunc   ;==>_Main
