#include <GDIPlus.au3>
#include <ScreenCapture.au3>

_Main()

Func _Main()
	Local $hImage, $sCLSID, $tData, $tParams

	; Capture screen
	_ScreenCapture_Capture(@MyDocumentsDir & "\GDIPlus_Image.jpg")

	; Initialize GDI+ library
	_GDIPlus_Startup()

	; Load image
	$hImage = _GDIPlus_ImageLoadFromFile(@MyDocumentsDir & "\GDIPlus_Image.jpg")

	; Get JPEG encoder CLSID
	$sCLSID = _GDIPlus_EncodersGetCLSID("JPG")

	; Set up parameters for 90 degree rotation
	$tData = DllStructCreate("int Data")
	DllStructSetData($tData, "Data", $GDIP_EVTTRANSFORMROTATE90)
	$tParams = _GDIPlus_ParamInit(1)
	_GDIPlus_ParamAdd($tParams, $GDIP_EPGTRANSFORMATION, 1, $GDIP_EPTLONG, DllStructGetPtr($tData, "Data"))

	; Save image with rotation
	_GDIPlus_ImageSaveToFileEx($hImage, @MyDocumentsDir & "\GDIPlus_Image2.jpg", $sCLSID, DllStructGetPtr($tParams))

	; Shut down GDI+ library
	_GDIPlus_Shutdown()

EndFunc   ;==>_Main
