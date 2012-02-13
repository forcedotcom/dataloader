#include <GDIPlus.au3>
#include <ScreenCapture.au3>

; ===============================================================================================================================
; Description ...: Shows how to rotate an image
; Author ........: Paul Campbell (PaulIA)
; Notes .........:
; ===============================================================================================================================

; ===============================================================================================================================
; Global variables
; ===============================================================================================================================
Global $hBitmap, $hImage, $sCLSID, $tData, $tParams

; ===============================================================================================================================
; Main
; ===============================================================================================================================

; Capture screen
_ScreenCapture_Capture(@MyDocumentsDir & '\AutoItImage.jpg')

; Initialize GDI+ library
_GDIPlus_Startup()

; Load image
$hImage = _GDIPlus_ImageLoadFromFile(@MyDocumentsDir & '\AutoItImage.jpg')

; Get JPG encoder CLSID
$sCLSID = _GDIPlus_EncodersGetCLSID("JPG")

; Set up parameters for 90 degree rotation
$tData = DllStructCreate("int Data")
DllStructSetData($tData, "Data", $GDIP_EVTTRANSFORMROTATE90)
$tParams = _GDIPlus_ParamInit(1)
_GDIPlus_ParamAdd($tParams, $GDIP_EPGTRANSFORMATION, 1, $GDIP_EPTLONG, DllStructGetPtr($tData, "Data"))

; Save image with rotation
_GDIPlus_ImageSaveToFileEx($hImage, @MyDocumentsDir & '\AutoItImage2.jpg', $sCLSID, DllStructGetPtr($tParams))

; Shut down GDI+ library
_GDIPlus_Shutdown()

; Show image
Run("MSPaint.exe " & '"' & @MyDocumentsDir & '\AutoItImage2.jpg"')
