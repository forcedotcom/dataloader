#include <GDIPlus.au3>
#include <ScreenCapture.au3>
#include <WinAPI.au3>

_Main()

Func _Main()
	Local $hBitmap1, $hBitmap2, $hImage1, $hImage2, $hGraphic, $width, $height

	; Initialize GDI+ library
	_GDIPlus_Startup()

	; Capture full screen
	$hBitmap1 = _ScreenCapture_Capture("")
	$hImage1 = _GDIPlus_BitmapCreateFromHBITMAP($hBitmap1)

	; Capture screen region
	$hBitmap2 = _ScreenCapture_Capture("", 0, 0, 400, 300)
	$hImage2 = _GDIPlus_BitmapCreateFromHBITMAP($hBitmap2)

	$width = _GDIPlus_ImageGetWidth($hImage2)
	$height = _GDIPlus_ImageGetHeight($hImage2)

	; Draw one image in another
	$hGraphic = _GDIPlus_ImageGetGraphicsContext($hImage1)

	;DrawInsert($hGraphic, $hImage2, $iX, $iY, $nAngle,    $iWidth,    $iHeight, $iARGB = 0xFF000000, $nWidth = 1)
	DrawInsert($hGraphic, $hImage2, 350, 100, 0, $width + 2, $height + 2, 0xFFFF8000, 2)
	DrawInsert($hGraphic, $hImage2, 340, 50, 15, 200, 150, 0xFFFF8000, 4)
	DrawInsert($hGraphic, $hImage2, 310, 30, 35, $width + 4, $height + 4, 0xFFFF00FF, 4)
	DrawInsert($hGraphic, $hImage2, 320, 790, -35, $width, $height)

	; Save resultant image
	_GDIPlus_ImageSaveToFile($hImage1, @MyDocumentsDir & "\GDIPlus_Image.jpg")

	; Clean up resources
	_GDIPlus_ImageDispose($hImage1)
	_GDIPlus_ImageDispose($hImage2)
	_WinAPI_DeleteObject($hBitmap1)
	_WinAPI_DeleteObject($hBitmap2)
	; Shut down GDI+ library
	_GDIPlus_Shutdown()

EndFunc   ;==>_Main

; #FUNCTION# ==================================================================================================
;Name...........: DrawInsert
; Description ...: Draw one image in another
; Syntax.........: DrawInsert($hGraphic, $hImage2, $iX, $iY, $nAngle, $iWidth, $iHeight, $iARGB = 0xFF000000, $nWidth = 1)
; inserts Graphics $hImage2 into $hGraphic
; Parameters ....: $hGraphics   - Handle to a Graphics object
;                  $hImage      - Handle to an Image object to be inserted
;                  $iX          - The X coordinate of the upper left corner of the inserted image
;                  $iY          - The Y coordinate of the upper left corner of the inserted image
;                  $iWidth      - The width of the rectangle Border around insert
;                  $iHeight     - The height of the rectangle Border around insert
;                  $iARGB       - Alpha, Red, Green and Blue components of pen color - Border colour
;                  $nWidth      - The width of the pen measured in the units specified in the $iUnit parameter - Border Width

; Return values .: Success      - True
;                  Failure      - False
;==================================================================================================
Func DrawInsert($hGraphic, $hImage2, $iX, $iY, $nAngle, $iWidth, $iHeight, $iARGB = 0xFF000000, $nWidth = 1)
	Local $hMatrix, $hPen2

	;Rotation Matrix
	$hMatrix = _GDIPlus_MatrixCreate()
	_GDIPlus_MatrixRotate($hMatrix, $nAngle, "False")
	_GDIPlus_GraphicsSetTransform($hGraphic, $hMatrix)

	_GDIPlus_GraphicsDrawImage($hGraphic, $hImage2, $iX, $iY)

	;get pen + color
	$hPen2 = _GDIPlus_PenCreate($iARGB, $nWidth)

	; Draw a frame around the inserted image
	_GDIPlus_GraphicsDrawRect($hGraphic, $iX, $iY, $iWidth, $iHeight, $hPen2)

	; Clean up resources
	_GDIPlus_MatrixDispose($hMatrix)
	_GDIPlus_PenDispose($hPen2)
	Return 1
EndFunc   ;==>DrawInsert
