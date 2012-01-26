#include <ScreenCapture.au3>

; Capture full screen
_ScreenCapture_Capture(@MyDocumentsDir & "\GDIPlus_Image1.jpg")

; Capture region
_ScreenCapture_Capture(@MyDocumentsDir & "\GDIPlus_Image2.jpg", 0, 0, 796, 596)

