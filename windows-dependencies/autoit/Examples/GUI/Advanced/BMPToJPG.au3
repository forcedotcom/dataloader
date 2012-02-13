#include <GDIPlus.au3>

; ===============================================================================================================================
; Description ...: Shows how to convert a BMP file to JPG
; Author ........: Paul Campbell (PaulIA)
; Notes .........:
; ===============================================================================================================================

; ===============================================================================================================================
; Global variables
; ===============================================================================================================================
Global $sImage, $hImage, $sCLSID

; Get BMP file to convert
$sImage = InputBox("BMP to JPG", "Enter File Name:", @MyDocumentsDir & "\Image.bmp", "", 200, 130)
If @error Or Not FileExists($sImage) Then Exit

; Initialize GDI+ library
_GDIPlus_Startup()

; Load image
$hImage = _GDIPlus_ImageLoadFromFile($sImage)

; Get JPG encoder CLSID
$sCLSID = _GDIPlus_EncodersGetCLSID("JPG")

; Save image as JPG
_GDIPlus_ImageSaveToFileEx($hImage, @MyDocumentsDir & "\AutoItImage.jpg", $sCLSID)

; Shut down GDI+ library
_GDIPlus_Shutdown()
