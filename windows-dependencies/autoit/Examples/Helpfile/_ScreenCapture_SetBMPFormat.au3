#include <ScreenCapture.au3>

; Capture full screen
_ScreenCapture_SetBMPFormat(0)
_ScreenCapture_Capture(@MyDocumentsDir & "\GDIPlus_Image.bmp")
