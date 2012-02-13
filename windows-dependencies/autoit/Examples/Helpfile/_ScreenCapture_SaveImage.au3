#include <ScreenCapture.au3>

_Main()

Func _Main()
	Local $hBmp

	; Capture full screen
	$hBmp = _ScreenCapture_Capture("")

	; Save bitmap to file
	_ScreenCapture_SaveImage(@MyDocumentsDir & "\GDIPlus_Image.jpg", $hBmp)

EndFunc   ;==>_Main
