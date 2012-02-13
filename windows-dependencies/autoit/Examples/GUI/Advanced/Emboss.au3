#include <GDIPlus.au3>
#include <ScreenCapture.au3>

; ===============================================================================================================================
; Description ...: Shows how to emboss text on an image
; Author ........: Paul Campbell (PaulIA)
; Notes .........:
; ===============================================================================================================================

; ===============================================================================================================================
; Global variables
; ===============================================================================================================================
Global $hBitmap, $hImage, $hGraphic, $hFamily, $hFont, $tLayout, $hFormat, $aInfo, $hBrush1, $hBrush2, $iWidth, $iHeight, $hPen
Global $sString = "  Created with AutoIt  "

; ===============================================================================================================================
; Main
; ===============================================================================================================================

; Initialize GDI+ library
_GDIPlus_Startup()

; Capture screen
$hBitmap = _ScreenCapture_Capture(@MyDocumentsDir & '\AutoItImage.bmp')

; Load image and emboss text
$hImage = _GDIPlus_ImageLoadFromFile(@MyDocumentsDir & '\AutoItImage.bmp')
$hGraphic = _GDIPlus_ImageGetGraphicsContext($hImage)
$hFamily = _GDIPlus_FontFamilyCreate("Arial")
$hFont = _GDIPlus_FontCreate($hFamily, 16, 1)
$tLayout = _GDIPlus_RectFCreate(0, 0)
$hFormat = _GDIPlus_StringFormatCreate(2)
$hBrush1 = _GDIPlus_BrushCreateSolid(0xA2FFFFFF)
$hBrush2 = _GDIPlus_BrushCreateSolid(0xC4FF0000)
$hPen = _GDIPlus_PenCreate(0xC4000000, 2)
$aInfo = _GDIPlus_GraphicsMeasureString($hGraphic, $sString, $hFont, $tLayout, $hFormat)
$iWidth = DllStructGetData($aInfo[0], "Width")
$iHeight = DllStructGetData($aInfo[0], "Height")

_GDIPlus_GraphicsFillRect($hGraphic, 0, 0, $iWidth, $iHeight, $hBrush1)
_GDIPlus_GraphicsDrawRect($hGraphic, 1, 1, $iWidth, $iHeight, $hPen)
_GDIPlus_GraphicsDrawStringEx($hGraphic, $sString, $hFont, $aInfo[0], $hFormat, $hBrush2)

; Save image
_GDIPlus_ImageSaveToFile($hImage, @MyDocumentsDir & '\AutoItImage2.bmp')

; Free resources
_GDIPlus_PenDispose($hPen)
_GDIPlus_BrushDispose($hBrush1)
_GDIPlus_BrushDispose($hBrush2)
_GDIPlus_StringFormatDispose($hFormat)
_GDIPlus_FontDispose($hFont)
_GDIPlus_FontFamilyDispose($hFamily)
_GDIPlus_GraphicsDispose($hGraphic)
_GDIPlus_ImageDispose($hImage)
_GDIPlus_Shutdown()

; Show image
Run("MSPaint.exe " & '"' & @MyDocumentsDir & '\AutoItImage2.bmp"')
